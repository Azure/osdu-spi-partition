package org.opengroup.osdu.service;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.opengroup.osdu.model.exception.AppException;

@ApplicationScoped
public class DirectoryWatchService {
  private static final Logger log = Logger.getLogger(DirectoryWatchService.class);
  private final ScheduledExecutorService scheduledExecutorService =
      Executors.newSingleThreadScheduledExecutor();
  private final Map<Runnable, ScheduledFuture<?>> debounceTasks = new ConcurrentHashMap<>();

  @ConfigProperty(name = "directory-watch.debounce-delay-ms", defaultValue = "300")
  int debounceDelayMs;

  public void watchDirectories(List<String> directories, Runnable runnable) {
    log.infof("Watching specified directories. Debounce delay: %s ms", debounceDelayMs);

    try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
      for (String directory : directories) {
        registerDirectoryTreeForWatching(Paths.get(directory), watchService);
      }

      while (!Thread.currentThread().isInterrupted()) {
        WatchKey key = watchService.take();
        log.debug("Directory change detected");
        registerNewlyCreatedDirectories(key, watchService);
        debounce(runnable);
        key.reset();
      }
    } catch (IOException e) {
      throw new AppException(
          INTERNAL_SERVER_ERROR.code(),
          INTERNAL_SERVER_ERROR.reasonPhrase(),
          "Error creating watch service: " + e,
          e);
    } catch (InterruptedException e) {
      log.error("Directory watch service interrupted. Exiting program.", e);
      Thread.currentThread().interrupt();
      System.exit(0);
    }
  }

  private void registerDirectoryTreeForWatching(Path directoryPath, WatchService watchService)
      throws IOException {
    Files.walkFileTree(
        directoryPath,
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
              throws IOException {
            dir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
            log.debugf("Registered directory for watching: %s", dir);
            return FileVisitResult.CONTINUE;
          }
        });
  }

  private void registerNewlyCreatedDirectories(WatchKey key, WatchService watchService) {
    Path dir = (Path) key.watchable();
    for (WatchEvent<?> event : key.pollEvents()) {
      if (event.kind() == ENTRY_CREATE) {
        try {
          @SuppressWarnings("unchecked")
          WatchEvent<Path> pathWatchEvent = (WatchEvent<Path>) event;
          Path createdDirPath = dir.resolve(pathWatchEvent.context());
          if (Files.isDirectory(createdDirPath)) {
            registerDirectoryTreeForWatching(createdDirPath, watchService);
            log.infof("Registered new directory for watching: %s", createdDirPath);
          }
        } catch (Exception e) {
          log.error("Error processing ENTRY_CREATE event", e);
        }
      }
    }
  }

  protected void debounce(Runnable runnable) {
    ScheduledFuture<?> currentTask = debounceTasks.get(runnable);
    if (currentTask != null) {
      currentTask.cancel(false);
    }
    ScheduledFuture<?> newTask =
        scheduledExecutorService.schedule(runnable, debounceDelayMs, MILLISECONDS);
    debounceTasks.put(runnable, newTask);
  }
}

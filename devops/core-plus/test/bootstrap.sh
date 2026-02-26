#!/usr/bin/env bash

set -ex

substitute_values() {
    cat << EOF > ./valuesForSubstitution.json
{
"<DATA_PARTITION_ID>": "${DATA_PARTITION_ID}",
"<PARTITION_POSTGRESQL_DB_NAME>": "${PARTITION_POSTGRESQL_DB_NAME}",
"<PARTITION_POSTGRESQL_USERNAME>": "${PARTITION_POSTGRESQL_USERNAME}",
"<PARTITION_POSTGRESQL_PASSWORD>": "${PARTITION_POSTGRESQL_PASSWORD}"
}
EOF

    # shellcheck disable=SC2207
    KEYS=( $(jq -r 'keys_unsorted[]' ./valuesForSubstitution.json) )
    # shellcheck disable=SC2207
    VALUES=( $(jq -r 'values[]' ./valuesForSubstitution.json) )

    for i in "${!KEYS[@]}"; do
    find ./ -type f -exec sed -i -e "s/${KEYS[$i]}/${VALUES[$i]}/g" {} \;
    done

}

execute_sql_scripts() {
    export PGPASSWORD=${POSTGRESQL_PASSWORD}
    psql -h "${POSTGRESQL_HOST}" -U "${POSTGRESQL_USERNAME}" -p "${POSTGRESQL_PORT}" -f "devops/core-plus/test/bootstrap.sql"
}

# General connection variables
source devops/core-plus/test/validate-env.sh "DATA_PARTITION_ID"

# Service users and passwords
source devops/core-plus/test/validate-env.sh "PARTITION_POSTGRESQL_DB_NAME"
source devops/core-plus/test/validate-env.sh "PARTITION_POSTGRESQL_USERNAME"
source devops/core-plus/test/validate-env.sh "PARTITION_POSTGRESQL_PASSWORD"
source devops/core-plus/test/validate-env.sh "POSTGRESQL_HOST"
source devops/core-plus/test/validate-env.sh "POSTGRESQL_PORT"
source devops/core-plus/test/validate-env.sh "POSTGRESQL_USERNAME"
source devops/core-plus/test/validate-env.sh "POSTGRESQL_PASSWORD"
source devops/core-plus/test/validate-env.sh "POSTGRESQL_DATABASE"
substitute_values
execute_sql_scripts

echo "Done bootstrapping the env"

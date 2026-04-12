#!/bin/bash

# Debezium 컨넥터 등록 API 호출
echo "Registering Debezium Outbox Connector..."

curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" \
  http://localhost:8083/connectors/ \
  -d @register-outbox-connector.json

echo -e "\nSuccess!"

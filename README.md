build dependencies (mysql, ftp-server)
cd /doc-compose
docker compose up -d

test api
curl --location 'http://localhost:8080/v1/credit-documents/upload' \
--form 'document=@"/path/test.jpg"' \
--form 'payload="{
\"caseNo\": \"CASE000002\",
\"docType\": \"Invoice\",
\"docClass\": \"Finance\",
\"destinationCompanyCode\": \"COMP001\",
\"destinationOfficeCode\": \"OFFICE007\",
\"totalDocument\": 5,
\"docStatus\": \"PENDING\",
\"hireNo\": \"HIRE998877\",
\"createdDate\": \"2025-04-18T09:00:00\",
\"createdBy\": \"system_user\",
\"updatedDate\": \"2025-04-18T10:00:00\",
\"updatedBy\": \"admin_user\"
}";type=application/json'
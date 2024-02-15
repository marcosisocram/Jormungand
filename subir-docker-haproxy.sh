docker compose -f docker-compose-haproxy.yaml up --force-recreate -V -d
docker compose logs -t --follow > docker-compose.logs

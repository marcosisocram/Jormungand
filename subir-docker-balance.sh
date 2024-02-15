docker compose -f docker-compose-balance.yaml up --force-recreate -V -d
docker compose logs -t --follow > docker-compose.logs

ports=(7080 7081 7082 8080 8443 8444 8445)
echo "stopping running apps"
for port in "${ports[@]}"; do
    lsof -ti "tcp:$port" | xargs kill
done

echo "apps should be stopped now"


# 🤖 CONFIGURATION N8N POUR CHATBOT OPÉRATIONNEL

## 📋 PROBLÈME ACTUEL
n8n utilise uniquement POST, mais nos endpoints nécessitent PUT et DELETE.

## ✅ SOLUTION 1 : CONFIGURER N8N (RECOMMANDÉ)

### Workflow n8n : chatbot-crud

```json
{
  "nodes": [
    {
      "name": "Webhook",
      "type": "n8n-nodes-base.webhook",
      "parameters": {
        "path": "chatbot-crud",
        "responseMode": "responseNode",
        "options": {}
      }
    },
    {
      "name": "Switch",
      "type": "n8n-nodes-base.switch",
      "parameters": {
        "dataPropertyName": "intent",
        "rules": {
          "rules": [
            { "value": "CREATE", "output": 0 },
            { "value": "READ", "output": 1 },
            { "value": "UPDATE", "output": 2 },
            { "value": "DELETE", "output": 3 }
          ]
        }
      }
    },
    {
      "name": "HTTP Request - CREATE",
      "type": "n8n-nodes-base.httpRequest",
      "parameters": {
        "method": "POST",
        "url": "http://localhost:8080/api/public/conventions",
        "options": {
          "bodyContentType": "json"
        },
        "bodyParametersJson": "={{ JSON.stringify($json.entities) }}"
      }
    },
    {
      "name": "HTTP Request - READ",
      "type": "n8n-nodes-base.httpRequest",
      "parameters": {
        "method": "GET",
        "url": "http://localhost:8080/api/public/conventions"
      }
    },
    {
      "name": "HTTP Request - UPDATE",
      "type": "n8n-nodes-base.httpRequest",
      "parameters": {
        "method": "PUT",
        "url": "=http://localhost:8080/api/public/conventions/{{ $json.entities.id }}",
        "options": {
          "bodyContentType": "json"
        },
        "bodyParametersJson": "={{ JSON.stringify($json.entities) }}"
      }
    },
    {
      "name": "HTTP Request - DELETE",
      "type": "n8n-nodes-base.httpRequest",
      "parameters": {
        "method": "DELETE",
        "url": "=http://localhost:8080/api/public/conventions/{{ $json.entities.id }}"
      }
    }
  ]
}
```

## ✅ SOLUTION 2 : ENDPOINT POST UNIVERSEL (ALTERNATIVE)

Si vous ne pouvez pas modifier n8n, utilisez un endpoint POST qui gère tout.

### Dans PublicConventionController.java

```java
@PostMapping("/conventions/execute")
public ResponseEntity<Map<String, Object>> executeOperation(@RequestBody Map<String, Object> request) {
    String operation = (String) request.get("operation"); // "create", "read", "update", "delete"
    String id = (String) request.get("id");
    Map<String, Object> data = (Map<String, Object>) request.get("data");
    
    switch (operation) {
        case "create":
            return createConvention(data);
        case "read":
            return getAllConventions();
        case "update":
            return updateConvention(id, data);
        case "delete":
            return deleteConvention(id);
        default:
            // Erreur
    }
}
```

## 📊 ENDPOINTS DISPONIBLES

| Opération | Méthode | Endpoint | Body |
|-----------|---------|----------|------|
| CREATE | POST | `/api/public/conventions` | `{client, amount, username}` |
| READ | GET | `/api/public/conventions` | - |
| UPDATE | PUT | `/api/public/conventions/{id}` | `{amount, status, username}` |
| DELETE | DELETE | `/api/public/conventions/{id}` | - |

## 🧪 TESTS

### Test CREATE
```bash
curl -X POST http://localhost:8080/api/public/conventions \
  -H "Content-Type: application/json" \
  -d '{"client":"Alpha","amount":5000,"username":"admin"}'
```

### Test UPDATE
```bash
curl -X PUT http://localhost:8080/api/public/conventions/68f855fc64c2eb49fedecb7b \
  -H "Content-Type: application/json" \
  -d '{"amount":7000,"status":"PAID","username":"admin"}'
```

### Test DELETE
```bash
curl -X DELETE http://localhost:8080/api/public/conventions/68f855fc64c2eb49fedecb7b
```

## 🎯 RECOMMANDATION

**Utilisez SOLUTION 1** : Configurez n8n pour utiliser les bonnes méthodes HTTP (PUT, DELETE).

C'est la meilleure approche car elle respecte les standards REST et permet une meilleure maintenabilité.

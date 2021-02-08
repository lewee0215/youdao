# 服务注册信息
NamingService.getAllInstances(serviceName)
NamingService.selectOneHealthyInstance(serviceName);
```json
[
    {
        "instanceId": "10.93.87.169#8070#DEFAULT#DEFAULT_GROUP@@service-provider",
        "ip": "10.93.87.169",
        "port": 8070,
        "weight": 1,
        "healthy": true,
        "cluster": {
            "serviceName": null,
            "name": "",
            "healthChecker": {
                "type": "TCP"
            },
            "defaultPort": 80,
            "defaultCheckPort": 80,
            "useIPPort4Check": true,
            "metadata": {}
        },
        "service": null,
        "metadata": {
            "preserved.register.source": "SPRING_CLOUD"
        }
    }
]
```
# Projeto de Sistemas Distribuídos 2015-2016 #

Grupo de SD 60 - Campus Taguspark


Daniel Reis 81981 ddreis88@gmail.com

Francisco Maria 81965 fran6maria@gmail.com

Ostap Kozak 82535 ostap_95@hotmail.com

Repositório:
https://github.com/danielreis1/Upa-WebService.git

-------------------------------------------------------------------------------

## Instruções de instalação 


### Ambiente

[0] Iniciar sistema operativo

Linux



[1] Iniciar servidores de apoio

JUDDI:
```
juddi/bin/startup.sh
```

[2] Criar pasta temporária

```
mkdir upa
cd upa

```


[3] Obter código
git clone https://github.com/danielreis1/Upa-WebService.git 

```

[4] Instalar módulos de bibliotecas auxiliares

```
cd uddi-naming
mvn clean install

cd ws-handlers
mvn clean install
```

### Serviço CA

[1] Construir e executar **servidor**
	cd ca-ws
	mvn clean install
	mvn exec:java

	cd ca-ws-cli
	mvn clean
	mvn generate-sources install


-------------------------------------------------------------------------------

### Serviço TRANSPORTER

[1] Construir e executar **servidor**

```
cd transporter-ws
mvn clean install
mvn -Dws.i=1 exec:java
```
Construir segundo servidor do transporter
```
cd transporter-ws
mvn clean install
mvn -Dws.i=2 exec:java
````

[2] Construir **cliente** e executar testes

```
cd transporter-ws-cli
mvn clean install
```

-------------------------------------------------------------------------------

### Serviço BROKER

[1] Construir e executar **servidor**

```
cd broker-ws
mvn clean install
mvn -Dws.type=0 exec:java
```
[2] Construir e executar  **replica** 
	cd broker-ws
	mvn clean install
	mvn -Dws.type=1 exec:java

[3] Construir **cliente** e executar testes

```
cd broker-ws-cli
mvn clean install
```

...

-------------------------------------------------------------------------------
**FIM**

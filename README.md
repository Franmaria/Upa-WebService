# Projeto de Sistemas Distribuídos 2015-2016 #

Grupo de SD 60 - Campus Taguspark


Daniel Reis 81981 ddreis88@gmail.com

Francisco Maria 81965 fran6maria@gmail.com

Ostap Kozak 82535 ostap_95@hotmail.com

Repositório:
[tecnico-distsys/T_60-project](https://github.com/tecnico-distsys/T_60-project/)

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
mkdir t60
cd t60

```


[3] Obter código fonte do projeto (versão entregue)

```
git clone -b SD_R2 https://github.com/tecnico-softeng-distsys-2015/T_60-project.git 

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

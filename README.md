`eccentricity-computer` is a Java tool for computing eccentricity of a protocol SUT model relative to a specification, which is typically a set of happy flow sequences.

## Walkthrough

From within the tool parent folder, first install the tool using Maven, by running:
```
mvn install
```

Then run `eccentricity-computer` using the `run_experiments.py` script file
```
python3 run_experiments.py -p ${protocol} -r ${role} -s ${specification_type}
```
Where:
- ${protocol} is a protocol for which a folder should exist in the `experiments` folder containing all the SUT models for the protocol grouped into role folders, e.g., dtls
- ${role} is a role for which a folder should exist in the `experiments/${protocol}` folder containing the folders for all the SUT models for the role, e.g., client
- ${specification_type}  is the type of specification to use, e.g., happy_flows. The file corresponding to this type will be taken from `experiments/${protocol}/${role}/specification/` folder (for happy_flows, the file is `happy_flows`)

Running the above command will compute eccentricity for all models under `experiments/${protocol}/${role}` using the given specification.
Multiple protocols and roles can be specified. For example, the below command computes eccentricity for DTLS and TLS client and server models.

```
python3 run_experiments.py -p dtls tls -r client server -s happy_flows
```

## Reproducing experiments on DTLS, TLS, SSH and EDHOC

To reproduce experiments on the four protocols, simply run:

```
python3 run_experiments.py -p dtls tls ssh edhoc -r client server -s happy_flows -e results_four_protocols.csv
```

This will calculate eccentricity for client/server models included in `experiments` for all four protocols. 
A summary of results is exported to `results_four_protocols.csv`.

## Reproducing experiments on Janssen's TLS models.

To reproduce experiments by on the many TLS models learned by Janssen run:
```
python3 run_experiments.py -p TLS12 -s happy_flows -t -e results_janssen.csv
```

This will engage a separate mode of the script designed specifically eccentricity on Janssen's models.
Results are stored in `results_janssen.csv`.
The `happy_flows` specification for TLS (1.2) is stored in `experiments/tls-models-specification/TLS12/specification/happy_flows`.
Models are stored in `experiments/tls-models`, this time grouped first by implementation, then version, then protocol.
For example, the model for MbedTLS 2.10.0 located in the folder `experiments/tls-models/mbedtls/2.10.0/TLS12/`.
The models were taken as is from the Janssen's [GitHub repository](https://github.com/tlsprint/models).


`eccentricity-computer` is a basic Java tool for computing eccentricity of a protocol SUT relative to a set of happy flow paths.

## Walkthrough

From within the tool parent folder, first install the tool using Maven, by running:
```
mvn install
```

Then run `eccentricity-computer` using the `run_experiments.py` script file
```
python3 run_experiments.py -p ${protocol} -r ${role}
```
Where:
- ${protocol} is a protocol which is covered in the `experiments` folder, e.g., dtls
- ${role} is a role which is covered in the `experiments/${protocol}` folder, e.g., client



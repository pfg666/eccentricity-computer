import os.path
import subprocess
import argparse
import shutil
import csv

JAR_LOCATION=os.path.join("target","eccompute-1.0-SNAPSHOT.jar")
EXP_LOCATION="experiments"
HAPPY_FLOWS="happy_flows"
global java_path

class ExperimentResult:
    def __init__(self, library:str, ecc:int) -> None:
        self.library, self.ecc = library, ecc

class ExperimentGroupResult:
    def __init__(self, protocol:str, role:str, spec:str, expResults:list):
        self.protocol, self.role, self.spec = protocol, role, spec
        self.expResults = expResults
    
class NoEccentricityFoundException(Exception):
    """Eccentricity could not be extracted from STDOUT."""
    pass


def run_experiment(sut_model, happy_flow):
    arguments = [java_path, "-jar", JAR_LOCATION, "-m", sut_model, "-s", happy_flow]
    result = subprocess.run(arguments, capture_output=True, text=True)
    for line in result.stdout.splitlines():
        if "eccentricity" in line:
            return int(line.split(":")[1].strip())
    raise NoEccentricityFoundException

def run_protocol_role_experiments(protocol, role, folder_path):
    files = os.listdir(folder_path)
    sut_models = sorted([os.path.join(folder_path, model) for model in files if model.endswith("dot")])
    happy_flow=os.path.join(folder_path, HAPPY_FLOWS)
    print(f"Eccentricity experiments for protocol: {protocol}, role: {role}")
    for sut_model in sut_models:
        eccentricity = run_experiment(sut_model, happy_flow)
        sut = os.path.basename(sut_model)[0:-4]
        print(f"SUT model: {sut} eccentricity: {eccentricity}")

if __name__ == '__main__':
    java_path = shutil.which("java")
    if java_path is None:
        print("java not found in PATH")
        exit()
    if (not os.path.exists(JAR_LOCATION)):
        print(f"{JAR_LOCATION} not found. Have you run `mvn install`?")
        exit()
    parser = argparse.ArgumentParser(description='Utility for launching eccentricity experiments on using files from the experiments folder.')
    parser.add_argument('-p', "--protocols", required=False, nargs="+", default=["dtls"],  help="What protocols to consider.")
    parser.add_argument('-r', "--roles", required=False, nargs="+", default=["server"],  help="What roles to consider.")
    args = parser.parse_args()

    for protocol in args.protocols:
        for role in args.roles:
            exp_folder_path=os.path.join(EXP_LOCATION, protocol,role)
            run_protocol_role_experiments(protocol, role, exp_folder_path)
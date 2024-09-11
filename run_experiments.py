import os.path
import subprocess
import argparse
import shutil
import csv

JAR_LOCATION = os.path.join("target","eccompute-1.0-SNAPSHOT.jar")
EXP_LOCATION = "experiments"
HAPPY_FLOWS = "happy_flows"
DFA_BASIS = "dfa_basis"
global java_path

class ExperimentResult:
    def __init__(self, model:str, ecc:int) -> None:
        self.model, self.ecc = model, ecc

class ExperimentGroupResult:
    def __init__(self, protocol:str, role:str, spec:str, expResults:list):
        self.protocol, self.role, self.spec = protocol, role, spec
        self.expResults = expResults
    
class NoEccentricityFoundException(Exception):
    """Eccentricity could not be extracted from STDOUT."""
    pass

class ExperimentRunner:
    def __init__(self, spec_type, added_args, verbose):
        self.__added_args = added_args
        self.__spec_type = spec_type
        self.__verbose = verbose
    
    def run_experiment(self, sut_model, spec_folder):
        arguments = [java_path, "-jar", JAR_LOCATION, "-m", sut_model]
        if self.__spec_type == HAPPY_FLOWS:
            arguments.extend(["-t", "HAPPY_FLOWS", "-s", os.path.join(spec_folder, 'happy_flows')])
        elif self.__spec_type == DFA_BASIS:
            arguments.extend(["-t", "DFA_BASIS", "-s", os.path.join(spec_folder, 'dfa_basis.dot')])

        arguments.extend(self.__added_args)
        if (self.__verbose):
            print(" ".join(arguments))
        result = subprocess.run(arguments, capture_output=True, text=True)
        lines = result.stdout.splitlines()
        #print(lines)
        if len(lines) >= 2 and "eccentricity" in lines[-1]:
            if (self.__verbose):
                print(lines[-2])
            return int(lines[-1].split(":")[1].strip())
        else: 
            raise NoEccentricityFoundException

def run_protocol_role_experiments(protocol, role, folder_path, runner:ExperimentRunner):
    files = os.listdir(folder_path)
    sut_models = sorted([os.path.join(folder_path, model) for model in files if model.endswith("dot")])
    print(f"Eccentricity experiments for protocol: {protocol}, role: {role}")
    for sut_model in sut_models:
        eccentricity = runner.run_experiment(sut_model, os.path.join(folder_path, "specification"))
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
    parser.add_argument('-s', "--specification_type", type=str, required=True, choices=[DFA_BASIS, HAPPY_FLOWS],  help="What type of specification to use")
    parser.add_argument('-a', "--additional_arguments", required=False, nargs="+", default=[], help="Additional arguments")
    parser.add_argument('-v', "--verbose", required=False, action="store_true",  help="Include more output (e.g., access sequences)")
    args = parser.parse_args()

    for protocol in args.protocols:
        for role in args.roles:
            exp_folder_path=os.path.join(EXP_LOCATION, protocol,role)
            if (os.path.exists(exp_folder_path)):
                run_protocol_role_experiments(protocol, role, exp_folder_path, ExperimentRunner(args.specification_type, args.additional_arguments, args.verbose))
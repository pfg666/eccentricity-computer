import os.path
import subprocess
import argparse
import shutil
import csv

JAR_LOCATION = os.path.join("target","eccompute-1.0-SNAPSHOT.jar")
EXP_LOCATION = "experiments"
TLS_MODELS_LOCATION = os.path.join("experiments", "tls-models")
TLS_MODELS_SPEC_LOCATION = os.path.join("experiments", "tls-models-specifications")
HAPPY_FLOWS = "happy_flows"
DFA_BASIS = "dfa_basis"
global java_path

class ExperimentResult:
    def __init__(self, ecc:int = None, alpha_size:int = None, basis_size:int = None, sut_model_size:int = None) -> None:
        self.ecc, self.alpha_size, self.basis_size, self.sut_model_size = ecc, alpha_size, basis_size, sut_model_size
        self.sut_desc = None
        self.role = None
        self.protocol = None
        self.alpha_desc = "noinfo"
    
    def set_sut_desc(self, sut_desc:str):
        self.sut_desc = sut_desc
    
    def add_info(self, protocol:str, role:str, sut_desc:str, alpha_desc:str = None):
        self.protocol = protocol
        self.role = role
        self.sut_desc = sut_desc
        if alpha_desc:
            self.alpha_desc = alpha_desc

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
    
    def run_experiment(self, sut_model, spec_folder) -> ExperimentResult:
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
        result =  self.parse_result(lines)
        if result is None:
            print("Failed to find eccentricity")
            print(f"Command: {' '.join(arguments)}")
            raise NoEccentricityFoundException
        else:
            return result
    
    def parse_result(self, lines) -> ExperimentResult:
        ecc, alpha_size, basis_size, sut_model_size = None, None, None, None
        for line in lines:
            stat_split = line.split(":")
            if len(stat_split) == 2:
                stat = stat_split[0].strip()
                stat_val = stat_split[1].strip()
                if stat == "Eccentricity":
                    ecc = int(stat_val)
                elif "Alphabet Size" in stat:
                    alpha_size = int(stat_val)
                elif "Basis Size" in stat:
                    basis_size = int(stat_val)
                elif "SUT Model Size" in stat:
                    sut_model_size = int(stat_val)
        if ecc is None:
            return None
        return ExperimentResult(ecc=ecc, alpha_size=alpha_size, 
                                basis_size=basis_size, 
                                sut_model_size=sut_model_size)

def extract_alpha_info(protocol, sut):
    if protocol == "dtls":
        parts = sut.split("_")[1:]
        if parts[0] == "client" or parts[0] == "server":
            parts = parts[1:]
        return "_".join(parts)
    if protocol == "tls":
        parts = sut.split("_")
        return parts[-1]
    return "noinfo"


def run_protocol_role_experiments(protocol, role, folder_path, runner:ExperimentRunner):
    results = list()
    files = os.listdir(folder_path)
    sut_models = sorted([os.path.join(folder_path, model) for model in files if model.endswith("dot")])
    protocol_specification =  os.path.join(folder_path, "specification")
    print(f"Eccentricity experiments for protocol: {protocol}, role: {role}")
    for sut_model in sut_models:
        result = runner.run_experiment(sut_model, protocol_specification)
        sut = os.path.basename(sut_model)[0:-4]
        print(f"SUT Model Name: {sut}, Eccentricity: {result.ecc}, Alphabet Size: {result.alpha_size}, SUT Model Size: {result.sut_model_size}, Basis Size: {result.basis_size}")
        result.add_info(protocol, role, sut, extract_alpha_info(protocol, sut))
        results.append(result)
    return results

def run_tls_models_experiments(protocol, runner:ExperimentRunner):
    results = list()
    protocol_specification = os.path.join(TLS_MODELS_SPEC_LOCATION, protocol)
    for sut in os.listdir(TLS_MODELS_LOCATION):
        sut_folder = os.path.join(TLS_MODELS_LOCATION, sut)
        for version in sorted(os.listdir(sut_folder)):
            version_folder = os.path.join(sut_folder, version)
            protocol_folder = os.path.join(version_folder, protocol)
            sut_model = os.path.join(protocol_folder, "learnedModel.dot")
            if os.path.exists(sut_model):
                result = runner.run_experiment(sut_model, protocol_specification)
                print(f"SUT: {sut}-{version}, Eccentricity: {result.ecc}, Alphabet Size: {result.alpha_size}, SUT Model Size: {result.sut_model_size}, Basis Size: {result.basis_size}")
                result.add_info(protocol, "server", f"{sut}-{version}")
                results.append(result)
            #break
        #break
    return results

def export_to_csv(results: list, name: str):
    with open(name, 'w') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow(["Protocol", "Role", "Alphabet Info", "Alphabet Size", "SUT", "SUT Model Size", "Basis Size", "Eccentricity"])
        for result in results:
            writer.writerow([result.protocol, result.role, 
                             result.alpha_desc, result.alpha_size, 
                             result.sut_desc, result.sut_model_size, 
                             result.basis_size, result.ecc])

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
    parser.add_argument('-t', "--tls_models", required=False, action="store_true",  help="Run instead on Erwin Janssen's TLS models")
    parser.add_argument('-e', "--export", required=False, type=str,  help="Export to CSV  format")
    args = parser.parse_args()
    results = list()

    if not args.tls_models:
        for protocol in args.protocols:
            for role in args.roles:
                exp_folder_path=os.path.join(EXP_LOCATION, protocol,role)
                if (os.path.exists(exp_folder_path)):
                    results = results + run_protocol_role_experiments(protocol, role, exp_folder_path, ExperimentRunner(args.specification_type, args.additional_arguments, args.verbose))
    else:
        for protocol in args.protocols:
            protocol = protocol.upper()
            results = results + run_tls_models_experiments(protocol, ExperimentRunner(args.specification_type, args.additional_arguments, args.verbose))
    
    if args.export and results:
        results = sorted(results, key = lambda r: (r.protocol, r.role, r.alpha_desc, r.sut_desc))
        export_to_csv(results, args.export)

import os.path
import subprocess
import argparse

JAR_LOCATION="target/eccompute-1.0-SNAPSHOT.jar"
EXP_LOCATION="experiments"
JAVA_PATH="/bin/java"

def run_experiment(sut_model, happy_flow, java_path=JAVA_PATH):
    #print(f"running {sut_model} {happy_flow}")
    arguments = [java_path, "-jar", JAR_LOCATION, sut_model, happy_flow]
    result = subprocess.run(arguments, capture_output=True, text=True)
    return result.stdout.splitlines()[0].split()[2]

def run_protocol_role_experiments(protocol, role, folder_path):
    files = os.listdir(folder_path)
    sut_models = sorted([os.path.join(folder_path, model) for model in files if model.endswith("dot")])
    happy_flow=os.path.join(folder_path,"happy_flows")
    print(f"Eccentricity experiments for protocol: {protocol}, role: {role}")
    for sut_model in sut_models:
        eccentricity = run_experiment(sut_model, happy_flow)
        sut = os.path.basename(sut_model)[0:-4]
        print(f"SUT: {sut} eccentricity: {eccentricity}")

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Utility for launching eccentricity experiments on using files from the experiments folder.')
    parser.add_argument('-p', "--protocols", required=False, nargs="+", default=["dtls"],  help="What protocols to consider.")
    parser.add_argument('-r', "--roles", required=False, nargs="+", default=["server"],  help="What roles to consider.")
    args = parser.parse_args()

    for protocol in args.protocols:
        for role in args.roles:
            exp_folder_path=os.path.join(EXP_LOCATION,protocol,role)
            run_protocol_role_experiments(protocol, role, exp_folder_path)
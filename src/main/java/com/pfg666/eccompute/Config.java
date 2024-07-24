package com.pfg666.eccompute;

import com.beust.jcommander.Parameter;

public class Config {
	
	@Parameter(names = {"-t", "-type"}, required = false, description = "The type of specification from which to derive access sequences.")
	private SpecificationType specificationType = SpecificationType.HAPPY_FLOWS;
	
	@Parameter(names = {"-s", "-specification"}, required = false, description = "Specification.")
	private String specification;
	
	@Parameter(names = {"-m", "-model"}, required = true, description = "Mealy machine of the SUT in DOT format.")
    private String model;

    public SpecificationType getSpecificationType() {
		return specificationType;
	}

	public String getSpecification() {
		return specification;
	}

	public String getModel() {
		return model;
	}

}

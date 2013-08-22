package com.mediasmiths.std.config;


public class IncompleteConfigurationDefinitionError extends ConfigurationFailureError {
	// private Field missingField;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public IncompleteConfigurationDefinitionError(IContextValueProvider provider, Throwable t) {
		super("Required field not defined: " + provider.getContextForErrorLog() + ". Error: " + t.getMessage(), t);
	}


	public IncompleteConfigurationDefinitionError(IContextValueProvider provider) {
		super("Required field not defined: " + provider.getContextForErrorLog());
	}

	/*

		public IncompleteConfigurationDefinitionError(Field f, String context) {
			super("Field " + f.getName() + " of class " + f.getDeclaringClass() + " is required but not present. Field context: " +
			    context);

			this.missingField = f;
		}


		public Field getMissingField() {
			return missingField;
		}//*/
}

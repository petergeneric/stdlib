// When called with an element ref (or selector), creates a new ConstraintUI within that element
// If called again, returns the same ConstraintUI object, allowing for further manipulation
window.constraints = function(el, schema, configurator) {
	if (el === undefined || el == null) {
		throw "No element or element path provided for constraints UI!";
	}
	else {
		var element = $(el);
		var ui = element.data('constraints');

		if (ui === undefined) {
			ui = new ConstraintUI(element, schema, configurator);

			element.data('constraints', ui);
		}

		return ui;
	}
}

function ConstraintUI(element, schema, configurator) {
	this.element = element;
	this.schema = schema;
	this.allowUnknownFields = true;
	this.formMethod = 'GET';
	this.formEndpoint = null;
	this.styling = {
		ascdesc: 'span1',
		input: null,
		rangeinput_from: 'span1',
		rangeinput_to: 'span1',
		functionselect: 'span2',
		fieldselect: null,
		enumselect: null
	};

	element.append('<h3>Constraints</h3><ul class="constraint-list"><li>New Constraint: <span class="field-add-ui"></span></li></ul> <br />  <h3>Order</h3><ul class="order-list"><li>Add order: <span class="field-add-ui"></span></li></ul></select>');

	this.constraintListElement = element.find("ul.constraint-list");
	this.orderListElement = element.find("ul.order-list");

	// Give the configurator a chance to configure us
	if (configurator)
	{
		if ('apply' in configurator)
			configurator.apply(this);
	
		if ('styling' in configurator)
			this.styling = configurator.styling;
	}
		
	// Populate constraint add UI
	var constraintAddDiv = this.constraintListElement.find(".field-add-ui");
	constraintAddDiv.append(this.renderFields(null, true));

	var self = this;

	constraintAddDiv.find("select.field-list").change(function() {
		var fieldName = $(this).val();

		if (fieldName != '') {
			/* De-select ready for next use */
			$(this).val('');

			self.addConstraint(fieldName, "eq", "");
		}
	});

	// Populate add order UI
	var orderAddDiv = this.orderListElement.find(".field-add-ui");
	orderAddDiv.html(this.renderFields(null, true));
	orderAddDiv.find("select.field-list").change(function() {
		var fieldName = $(this).val();

		if (fieldName != '') {
			/* De-select ready for next use */
			$(this).val('');

			self.addOrder(fieldName);
		}
	});
}

// Render a field list UI
// Optionally with a selected field name
ConstraintUI.prototype.renderFields = function(selection, allowCustom) {
	var select = $('<select class="field-list"><option value=""></option></select>');

	select.addClass(this.styling.fieldselect);
	
	if (allowCustom === undefined)
		allowCustom = false;

	for (var key in this.schema) {
		var option = $('<option></option>');
		if ('caption' in this.schema[key])
			option.text(this.schema[key].caption);
		else
			option.text(key);

		option.val(key);

		if (key == selection)
			option.prop('selected', true);

		select.append(option);
	}

	if (allowCustom && this.allowUnknownFields) {
		select.append($('<option></option>').text('<custom field>').val('_custom'));
	}

	return select;
}

ConstraintUI.prototype.renderFunctions = function(fieldName) {
	var functions = [];
	{
		var schema = this.schema[fieldName];

		if (schema === undefined || schema == null)
			throw "No such field: " + fieldName;

		// Always allow eq and neq
		functions.push({name: "eq", text: "Is"});
		functions.push({name: "neq", text: "Is Not"});

		if (schema.nullable == true) {
			functions.push({name: "isNull", text: "Absent"});
			functions.push({name: "isNotNull", text: "Present"});
		}

		if (schema.type == 'string') {
			functions.push({name: "starts", text: "Starts"});
			functions.push({name: "contains", text: "Contains"});
		}

		if (schema.type == 'number' || schema.type == 'datetime' || schema.type == 'string') {
			functions.push({name: "range", text: "Between"});
			functions.push({name: "gt", text: '>'});
			functions.push({name: "lt", text: '<'});
			functions.push({name: "ge", text: '>='});
			functions.push({name: "le", text: '<='});
		}
	}

	var select = $('<select class="constraint_function"></select>');

	select.addClass(this.styling.functionselect);

	for (var i in functions) {
		var option = $('<option></option>');

		option.text(functions[i].text);
		option.val(functions[i].name);

		select.append(option);
	}

	return select;
}

ConstraintUI.prototype.addDummyField = function(fieldName) {
	if (!(fieldName in this.schema)) {
		this.schema[fieldName] = {
			type: 'string',
			nullable: true
		};
	}
}

//	returns HTML elements for the provided function (optionally with the default values filled in from argument)
ConstraintUI.prototype.renderInput = function(fieldName,functionName, argument) {

	if (fieldName === undefined || fieldName === null)
		throw "Must provide field name!";
	else if (functionName === undefined || functionName === null)
		throw "Must provide function!";

	// Either enforce that it must be a known field or dynamically add the unknown field
	if (!(fieldName in this.schema)) {
		if (this.allowUnknownFields)
			this.addDummyField(fieldName);
		else
			throw "No such field with name: " + fieldName;
	}


	var schema = this.schema[fieldName];

	if (schema == null && this.allowUnknownFields) {
		schema = {};
	}

	// Applies numeric validation to an input
	var ensureNumeric = function(input) {
		input.attr('required', true);
		input.attr('pattern', '(min|max|[0-9]+(\.[0-9]+)?)');
		input.attr('title', 'Expected: number, min or max)');
	}

	// Applies date validation to an input
	// This only approximates a valid date/time - the server will perform full validation on submit
	var ensureDate = function(input) {
		input.attr('required', true);

		// Year - Month - Day
		var date = '[0-9]{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])'
		var time = '[0-9]{2}:[0-9]{2}:[0-6][0-9]?(\.[0-9]{1,4})?';
		var timezone = '(|Z|[\-+][0-9:]+)'; // Approximate a valid timezone

		var datetime = date + '(T'+ time + timezone + ')?';

		// Allow period to be case insensitive
		// N.B. this only approximates an ISO8601 duration
		var period = '[Pp](([0-9]+[YMWDymwd])+|([0-9]+[YMWDymwd])*[Tt]([0-9\.]+[HMShms])+)';

		// Exact datetime OR an expression. In expressions space and plus are treated identically
		input.attr('pattern', '('+datetime+'|(now|today|tomorrow|yesterday|sow|som|soy)([- +]'+ period + ')?)');
		input.attr('title', 'Expected ISO8601 date, ISO8601 datetime or time expression (now, today, tomorrow, yesterday,sow,som,soy then optionally +/- ISO8601 period)');
	}

	var addValidation = function(dataType, input) {
		if (dataType == 'number')
			ensureNumeric(input);
		else if (dataType == 'datetime')
			ensureDate(input);
	}

	switch(functionName) {
		case "eq":
		case "neq":
		case "starts":
		case "contains":
		case "gt":
		case "ge":
		case "lt":
		case "le":
			// TODO implement some date/time picker logic for datetime fields?
			if (schema.type == 'enum' || schema.type == 'boolean') {
				var select;
				if (schema.type == 'boolean') {
					select = $('<select name="value"><option>true</option><option>false</option>');
				}
				else {
					select = $('<select name="value"></select>');

					for (var i in schema.values) {
						var value = schema.values[i];
						select.append($('<option>').text(value));
					}
				}

				if (argument !== undefined && argument !== null)
					select.val(argument);
					
				select.addClass(this.styling.enumselect);

				return select;
			}
			else {
				var input = $('<input name="value" type="text" />').val(argument);

				input.addClass(this.styling.input);
				
				addValidation(schema.type, input);

				return input;
			}
		case "range":
			if (argument == "" || argument == null)
				argument="..";

			var fields = argument.split("..",2);
			var from = $('<input name="from" type="text" />').val(fields[0]);
			var to = $('<input name="to" type="text" />').val(fields[1]);

			from.addClass(this.styling.rangeinput_from);
			to.addClass(this.styling.rangeinput_to);

			addValidation(schema.type, from);
			addValidation(schema.type, to);

			return $('<span />').append(from).append(' - ').append(to);
		case "isNull":
		case "isNotNull":
			return "";
		default:
			alert("Unknown function: " + functionName + "!");
	}
}

ConstraintUI.prototype.setConstraintFunction = function(constraintLI, functionName, argument) {
	var fieldName = $(constraintLI).closest("[data-field-name]").data("field-name");
	var functionSelect = $(constraintLI).find("select:first");
	var inputsSpan = $(constraintLI).find("span.inputs");

	if (!fieldName)
		throw "Could not extract field name from constraint LI";

	functionSelect.val(functionName);

	inputsSpan.empty();
	inputsSpan.append(this.renderInput(fieldName, functionName, argument));
}

ConstraintUI.prototype.encodeOrder = function(orderLI) {
	var fieldName = $(orderLI).find("select.field-list").val();
	var direction = $(orderLI).find("select[name=direction]").val();

	return fieldName + ' ' + direction;
}

ConstraintUI.prototype.getConstraints = function() {
	var self = this;

	var encodeInputs = function(inputsSpan, functionName, fieldName) {
		var schema = self.schema[fieldName];

		// TODO needs to be modified when intelligently rendering inputs based on data type

		switch(functionName) {
			case 'eq':
			case 'neq':
			case 'starts':
			case 'contains':
			case 'gt':
			case 'lt':
			case 'ge':
			case 'le':
				return $(inputsSpan).find('input[name="value"], select[name="value"]').val();
			case 'range':
				return $(inputsSpan).find('input[name="from"]').val() + '..' + $(inputsSpan).find('input[name="to"]').val();
			default:
				throw "Do not know how to extract value for function: " + functionName + " and field " + fieldName;
		}
	}

	var encodeConstraint = function(fieldName, constraintLI) {
		var functionSelect = $(constraintLI).find("select:first");
		var inputsSpan = $(constraintLI).find("span.inputs");

		var functionName = functionSelect.val();

		if (functionName == 'isNull')
			return '_null';
		else if (functionName == 'isNotNull')
			return '_notnull';
		else {
			var encodedValue = encodeInputs(inputsSpan, functionName, fieldName);

			// For eq, as long as the value doesn't begin with _ we can use a simpler notation
			if (functionName == 'eq' && encodedValue.charAt(0) != '_')
				return encodedValue;
			else
				return '_f_' + functionName + '_' + encodedValue;
		}
	}


	var encoded = {};
	this.constraintListElement.find('li[data-field-name]').each(function() {
		var fieldName = $(this).data("field-name");

		var values = [];

		$(this).find("li").each(function() {
			values[values.length] = encodeConstraint(fieldName, $(this));
		});

		encoded[fieldName] = values;
	});

	this.orderListElement.find("li.order-line").each(function() {
		if (!( '_order' in encoded)) {
			encoded['_order'] = [];
		}

		encoded['_order'].push(self.encodeOrder(this));
	});

	this.constraintListElement.find('input.literal-constraint').each(function() {
		var fieldName = $(this).attr('name');
		var value = $(this).val();

		if (fieldName === '_offset' && value == 0)
			return; // Don't encode an offset of 0

		if (!(fieldName in encoded)) {
			encoded[fieldName] = [];
		}

		encoded[fieldName].push(value);
	});

	return encoded;
}

ConstraintUI.prototype.setConstraints = function(encoded) {
	// Remove all constraints
	this.clear();

	// Add the encoded constraints
	for (var key in encoded) {
		var val = encoded[key];

		if (typeof val === 'string') {
			this.addConstraint(key, val);
		}
		else {
			for (var i in val) {
				this.addConstraint(key, val[i]);
			}
		}
	}
}

// Replace the ordering 
ConstraintUI.prototype.setOrder = function(fieldName, direction) {
	this.clearOrder();
	
	if (fieldName != null)
		this.addOrder(fieldName, direction);
}

ConstraintUI.prototype.addOrder = function(fieldName, direction) {
	if (direction === undefined || direction == null) {
		direction = "asc";
	}

	// If custom field name, prompt the user to supply it
	if (fieldName == '_custom') {
		fieldName = window.prompt("Please provide custom field name");
		this.addDummyField(fieldName);
	}
	else if (!(fieldName in this.schema)) {
		// Either enforce that it must be a known field or dynamically add the unknown field

		if(this.allowUnknownFields)
			this.addDummyField(fieldName);
		else
			throw "No such field with name: " + fieldName;
	}


	var existingElement = this.orderListElement.find('li.order-line > select.field-list').filter(function(i,e) {
		return ($(e).val() == fieldName);
	});

	var alreadyExists = existingElement.length > 0;

	if (!alreadyExists) {
		var insertionPoint = this.orderListElement.find("li:last-child");

		var ascDescSelect;
		{
			ascDescSelect = $('<select name="direction"></select>');

			ascDescSelect.addClass(this.styling.ascdesc);

			var a = $('<option>asc</option>');
			var d = $('<option>desc</option>');

			if (direction == 'asc')
				a.prop('selected', true);
			else if (direction == 'desc')
				d.prop('selected', true);

			ascDescSelect.append(a);
			ascDescSelect.append(d);
		}

		var el = $('<li class="order-line"></li>');

		el.append(this.renderFields(fieldName));
		el.append(ascDescSelect);

		// TODO customise how remove link is rendered?
		var removeLink;
		{
			removeLink = $('<a class="order-line_remove" href="#" shape="rect"><i class="icon-remove-circle"></i></a>');
			removeLink.click(function() {
				$(this).closest("li.order-line").remove();
			});
		}

		el.append(removeLink);

		el.insertBefore(insertionPoint);
	}
}

ConstraintUI.prototype.addConstraint = function(fieldName, functionName, argument) {
	if (argument === undefined && functionName != null) {
		var encoded = functionName;

		if (String(encoded).indexOf("_f_") == 0) {
			var fields = encoded.split("_", 4);

			functionName= fields[2];
			argument = fields[3];
		}
		else {
			functionName = null;
			argument = encoded;
		}
	}

	// Default the function name (N.B. take account of null/not null restrictions)
	if (functionName == null && argument == "_null")
		functionName = "isNull";
	if (functionName == null && argument == "_notnull")
		functionName = "isNotNull";
	else if (functionName == null)
		functionName = 'eq';

	// If custom field name, prompt the user to supply it
	if (fieldName == '_custom') {
		fieldName = window.prompt("Please provide custom field name");

		this.addDummyField(fieldName);
	}

	if (fieldName.indexOf("_") == 0) {
		if (fieldName == '_order') {
			// Call the addOrder logic
			var fields = argument.split(" ", 2);

			this.addOrder(fields[0], fields[1]);
		}
		else {
			// Special-case _offset and _limit fields, only allow at most one to exist
			if (fieldName == '_offset' || fieldName == '_limit') {

				var existingInput = this.constraintListElement.find('input.literal-constraint[name="' + fieldName + '"]');

				// Field does not exist yet, fall back on addition of new literal constraint

				if (existingInput.length > 0) {
					existingInput.val(argument);
					return;
				}
			}

			// Carry it as a literal field
			var input = $('<input class="literal-constraint" type="hidden" />');

			input.attr('name', fieldName);
			input.val(argument);

			this.constraintListElement.append(input);
		}
	}
	else {
		// Either enforce that it must be a known field or dynamically add the unknown field
		if (!(fieldName in this.schema)) {
			if(this.allowUnknownFields)
				this.addDummyField(fieldName);
			else
				throw "No such field with name: " + fieldName;
		}

		var schema = this.schema[fieldName];

		// TODO figure out if we have a field already. If we do already when skip the addition of the field entry
		var existingField = this.constraintListElement.find("li[data-field-name='"+fieldName+"']");

		if (existingField.length > 0) {
			// Find the UL
			var constraintUL = $(existingField).find("ul");

			var constraintLineLI = $('<li class="constraint-line"></li>');

			constraintLineLI.data('field-name', fieldName);

			var functionSelect = this.renderFunctions(fieldName);

			constraintLineLI.append(functionSelect);

			constraintLineLI.append($('<span class="inputs" /> <a class="constraint-line_remove" href="#" shape="rect"><i class="icon-remove-circle"></i></a>'));

			var self = this;
			functionSelect.change(function() {
				var constraintLI = $(this).closest("li");
				var functionName = $(this).val();

				self.setConstraintFunction(constraintLI, functionName, "");
			});

			constraintUL.append(constraintLineLI);

			// set the default value
			this.setConstraintFunction(functionSelect.parent(), functionName, argument);

			constraintUL.find("a.constraint-line_remove").click(function() {
				if ($(this).closest("li[data-field-name]").find("li.constraint-line").length == 1) {
					// This was the last constraint, remove this field's entry
					$(this).closest("li[data-field-name]").remove();
				}
				else {
					// Remove only this constraint line
					$(this).closest("li.constraint-line").remove();
				}
			});
		}
		else {
			var caption = ('caption' in schema) ? schema.caption : fieldName;
			var li = $('<li></li>');

			// N.B. using data('field-name', fieldName) breaks existingField selector!
			li.attr('data-field-name', fieldName);

			li.append(caption + ' matches any of: ');

			var removeLink = $('<a class="constraint_remove" href="#" shape="rect"><i class="icon-remove"></i> Remove</a>');
			var addLink = $('<a class="constraint_value_add" href="#" shape="rect" title="Add"><i class="icon-plus"></i> Add</a>');

			li.append(addLink);
			li.append(' ');
			li.append(removeLink);

			// Add empty UL for constraint lines
			li.append($('<ul></ul>'));

			// Add the constraint entry just before the "Add Constraint" UI
			li.insertBefore(this.constraintListElement.find("li:last"));

			removeLink.click(function() {
				$(this).closest("li[data-field-name]").remove();
			});

			var self = this;
			addLink.click(function() {
				var fieldName = $(this).closest("li[data-field-name]").data('field-name');

				self.addConstraint(fieldName);
			});

			this.addConstraint(fieldName,functionName,argument);
		}
	}
}

// Dynamically build up a form and submit it
// Optionally allows a method (e.g. GET/POST) to be supplied and an endpoint
// If not specified then it defaults to a GET to the current URL
ConstraintUI.prototype.submit = function(method, endpoint) {
	var form = $("<form/>");

	// Allow method to be customised (default to GET)
	if (method !== undefined)
		form.attr('method', method);
	else
		form.attr('method', this.formMethod);

	// Allow endpoint to be customised
	if (endpoint !== undefined)
		form.attr('action', endpoint);
	else if (this.formEndpoint !== null)
		form.attr('action', this.formEndpoint);

	var constraints = this.getConstraints();

	// Builds hidden input elements
	var inputFunction = function(name,value) {
		return $("<input />").attr('type','hidden').attr('name', name).val(value);
	};

	// Set up the inputs
	for (var key in constraints) {
		var value = constraints[key];

		if (typeof value === 'string') {
			form.append(inputFunction(key, value));
		}
		else {
			for (var i in value) {
				form.append(inputFunction(key, value[i]));
			}
		}
	}

	// Place the form on the page
	form.appendTo(document.body);

	// Now submit the form
	form.submit();
}

// Navigate to the next page
ConstraintUI.prototype.nextPage = function() {
	this.pageDelta(1);
}

// Navigate to the previous page
ConstraintUI.prototype.prevPage = function() {
	this.pageDelta(-1);
}

// Navigate to a page offset
ConstraintUI.prototype.pageDelta = function(delta) {
	var encoded = this.getConstraints();

	var offset = ('_offset' in encoded) ? encoded['_offset'] : 0;
	var limit = ('_limit' in encoded) ? encoded['_limit'] : 200;

	// Make sure we never go below zero (in the case of a negative delta)
	var newOffset = Math.max(0, Number(offset) + (delta * Number(limit)));

	if (newOffset != offset) {
		this.addConstraint('_offset', newOffset);

		this.submit();
	}
}

// Navigate to a specific page
ConstraintUI.prototype.page = function(num) {
	if (num < 0)
		throw "Page number must be positive integer";

	var encoded = this.getConstraints();

	var offset = ('_offset' in encoded) ? encoded['_offset'] : 0;
	var limit = ('_limit' in encoded) ? encoded['_limit'] : 200;

	this.addConstraint('_offset', Number(limit) * num);

	this.submit();
}

// Clear all constraints and ordering
ConstraintUI.prototype.clear = function() {
	this.clearConstraints();
	this.clearOrders();
}

// Remove all constraints
ConstraintUI.prototype.clearConstraints = function() {
	this.constraintListElement.find("li[data-field-name]").remove();
}

// Remove specific constraints
ConstraintUI.prototype.removeConstraint = function(fieldName) {
	if (fieldName == '_order') {
		this.orderListElement.find("li.order-line").remove();
	}
	else if (fieldName.indexOf('_') == 0) {
		// Remove a literal constraint
		this.constraintListElement.find("input.literal-constraint").filter(function(i,e) {
			var field = $(e).attr('name');
		
			return (field == fieldName);
		}).remove();
	}
	else {
		// Regular constraint
		this.constraintListElement.find("li[data-field-name]").filter(function(i,e) {
			var field = $(e).data('field-name');
			
			return (field == fieldName);
		}).remove();	
	}
}

// Remove all orderings (and, implicitly, the offset)
ConstraintUI.prototype.clearOrders = function() {
	this.removeConstraint('_offset');
	this.removeConstraint('_order');
}

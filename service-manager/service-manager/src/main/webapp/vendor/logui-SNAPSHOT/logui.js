LogUI.prototype.LEVEL_NAMES = ['0', '1', 'TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR', 'FATAL', '8', '9'];
LogUI.prototype.MAX_SCROLLBACK_SIZE = 10000;
LogUI.prototype.SHORT_TIMESTAMP_FORMAT = 'HH:mm:ss.SS';
LogUI.prototype.LONG_TIMESTAMP_FORMAT = 'D MMM HH:mm:ss.SS';


function LogUI(element, nonce, tailURL, searchURL) {
	var me = this;

	this.uiroot = $(element);
	this.nonce = nonce;
	this.tailTimeout = null;
	this.tailURL = tailURL;
	this.searchURL = searchURL;

	this.END_OF_LOG_TABLE = this.uiroot.find("#latest");
	this.logLinesInMemory = 0;
	this.lastTableRowMousedown = new Date(0);
	this.ALL_ENDPOINTS = {};
	this.ENDPOINT_RENDERER = this.endpointRenderShort;
	this.MESSAGE_RENDERER = this.messageRendererDefault;
	this.EVENT_FILTER = function () {
		return true;
	};
	this.OVERRIDE_ENDPOINTS = {};

	// Register listeners for the filter options being changed
	this.uiroot.find("#filter-level").dropdown('setting', 'onChange', function () {
		me.refilter();
	});
	this.uiroot.find("#filter-endpoint").dropdown('setting', 'onChange', function () {

		if (me.getFilteredEndpoints().length == 1)
			me.uiroot.find(".log_endpoint").hide();
		else if (me.getFilteredEndpoints().length == 0 || me.getFilteredEndpoints().length == 2)
			me.uiroot.find(".log_endpoint").show();

		me.refilter();
	});
}

// Start tailing logs using a particular subscription id
// N.B. this subscription id must already exist
LogUI.prototype.startTail = function (subscriptionId) {
	this.subscriptionId = subscriptionId;

	// TODO configure the UI for Tail
	this.uiroot.find("#searchCriteria").hide();

	this.getRecentLines();
};

// Search the logs and display the results
LogUI.prototype.search = function (start, end, minLevel, endpoint) {
	var me = this;

	// If there's an active tail timeout we should cancel it
	if (this.tailTimeout != null) {
		clearTimeout(this.tailTimeout);

		this.tailTimeout = null;
	}

	// Make sure we remove any lines from previous UI operations
	this.clearLines();

	// TODO if start/end/minLevel/endpoint specified then populate the UI
	this.uiroot.find("#searchCriteria").show();

	this.uiroot.find("#searchCriteriaExecute").click(function () {
		me.searchFromUI();
	});

	// Produce next/prev buttons
	var latestLine = this.uiroot.find("#latest td");
	latestLine.text("");

	var nextButton = $('<button class="btn">Next &raquo</button>');
	nextButton.click(function () {
		me.searchFromUI(1);
	});
	var prevButton = $('<button class="btn">Prev &laquo</button>');
	prevButton.click(function () {
		me.searchFromUI(-1);
	});

	latestLine.append(prevButton);
	latestLine.append(nextButton);
};

LogUI.prototype.searchFromUI = function (delta) {
	var from = this.uiroot.find("#searchFrom").val();
	var to = this.uiroot.find("#searchTo").val();
	var filter = this.uiroot.find("#searchFilter").val();

	if (delta !== undefined) {
		var fromDate = moment(from);
		var toDate = moment(to);

		// Produce a duration that's either positive or negative based on the delta value
		var duration = toDate.diff(fromDate) * delta;

		var from = fromDate.add(delta).format();
		var to = toDate.add(delta).format();
	}

	this.executeSearch(from, to, filter);
};

LogUI.prototype.executeSearch = function (start, end, filter) {
	var me = this;

	// Start and End are likely in datetime-local format (1970-01-01T00:00:00.00), they should be sent to the server as UTC dates
	var fromISO = moment(start).format();
	var toISO = moment(end).format();

	$.ajax({
		type: "POST",
		dataType: 'json',
		url: this.searchURL,
		data: {
			'from': fromISO,
			'to': toISO,
			'filter': filter
		},
		complete: function (data, textStatus) {
			if (textStatus == 'success') {
				me.addLines($.parseJSON(data.responseText));
			}
			else {
				var latestLine = me.uiroot.find("#latest td");

				if (data.getResponseHeader('X-Rich-Exception') == "1") {
					var failureXML = $.parseXML(data.responseText);

					if (failureXML) {
						failureXML = $(failureXML);

						var errorDetail;
						if (failureXML.find("detail").length > 0) {
							errorDetail = $(failureXML.find("detail")[0]).text();
						}
						else {
							errorDetail = $(failureXML.find("shortName")[0]).text();
						}

						latestLine.text("Server returned rich exception as a result of a search: " + errorDetail);
					}
					else {
						latestLine.text("Server returned rich exception that could not be parsed: " + data.responseText);
					}
				}
				else {
					latestLine.text("Unknown problem occurred as a result of a search: textStatus=" + textStatus + ". responseText=" + data.responseText);
				}
			}
		}
	});
};

LogUI.prototype.getRecentLines = function () {
	var me = this;

	$.ajax({
		type: "POST",
		dataType: 'json',
		url: this.tailURL,
		data: {
			'id': subscriptionId,
			'nonce': nonce
		},
		complete: function (data, textStatus) {
			if (textStatus == 'success') {
				me.addLines($.parseJSON(data.responseText));

				me.tailTimeout = setTimeout(function () {
					me.getRecentLines()
				}, 500);
			}
			else {
				var latestLine = me.uiroot.find("#latest td");

				if (data.getResponseHeader('X-Rich-Exception') == "1") {
					var failureXML = $.parseXML(data.responseText);

					if (failureXML) {
						failureXML = $(failureXML);

						var errorDetail;
						if (failureXML.find("detail").length > 0) {
							errorDetail = $(failureXML.find("detail")[0]).text();
						}
						else {
							errorDetail = $(failureXML.find("shortName")[0]).text();
						}

						latestLine.text("Server returned rich exception as a result of a log request: " + errorDetail);
					}
					else {
						latestLine.text("Server returned rich exception that could not be parsed: " + data.responseText);
					}
				}
				else {
					latestLine.text("Unknown problem occurred as a result of a log request: textStatus=" + textStatus + ". responseText=" + data.responseText);
				}
			}
		}
	});
};


LogUI.prototype.messageRendererDefault = function (event, td) {

	if (event.exceptionId && event.exceptionId.length > 0)
		td.text("!!" + event.exceptionId.substring(0, 4) + " " + event.message);
	else
		td.text(event.message);

	return td;
};

LogUI.prototype.endpointRenderShort = function (event) {
	var endpoint = event.endpoint;

	if (endpoint == "http://unknown") {
		return "";
	}
	else {
		// If the endpoint ends with /rest or /rest/ then we should remove that from it
		if (endpoint.endsWith("/rest/"))
			endpoint = endpoint.substring(0, endpoint.length - "/rest/".length);
		else if (endpoint.endsWith("/rest"))
			endpoint = endpoint.substring(0, endpoint.length - "/rest".length);

		var lastSlash = endpoint.lastIndexOf('/', endpoint.length - 1);

		if (lastSlash != -1)
			return endpoint.substring(lastSlash + 1);
		else
			return endpoint;
	}
};

LogUI.prototype.endpointRenderFull = function (event) {
	return event.endpoint;
};

LogUI.prototype.addLines = function (events) {
	if (events.length > 0) {
		// Allows us to keep track of whether the user was at the bottom of the page
		// If they were at the bottom of the page at the start of this method we should ensure they are at the end
		var isAtBottomOfPage = isElementInViewport(this.END_OF_LOG_TABLE);

		for (var i = 0; i < events.length; i++) {
			this.addLine(events[i]);
		}

		this.logLinesInMemory += events.length;

		// Keep within the scrollback buffer limit
		if (this.logLinesInMemory > this.MAX_SCROLLBACK_SIZE) {
			// Remove an additional 500 lines (so we aren't having to delete lines every time)
			var linesToRemove = (this.MAX_SCROLLBACK_SIZE - this.logLinesInMemory) + 500;

			this.clearLines(linesToRemove);
		}

		// Make sure to keep the last log line in view (if we were at the bottom of the page at the start of the method)
		if (isAtBottomOfPage) {
			this.END_OF_LOG_TABLE[0].scrollIntoView({
				behavior: "instant",
				block: "end"
			});
		}
	}
};

// Remove a set number of lines from the start of the buffer
// If no number of lines is supplied then remove all lines
LogUI.prototype.clearLines = function (linesToRemove) {
	if (linesToRemove === undefined || linesToRemove > this.logLinesInMemory) {
		this.uiroot.find("#loglines tr.logline").remove();
		this.logLinesInMemory = 0;
	}
	else {
		// Remove the number of lines requested
		this.uiroot.find("#loglines tr.logline").slice(0, linesToRemove).remove();

		this.logLinesInMemory -= linesToRemove;
	}
};

// Formats a timestamp string in the local timezone
// N.B. ignores the day if it's today
// This is useful for the tail page + searches within a single day
LogUI.prototype.formatTimestamp = function (when) {
	if (when.isSame(new Date(), 'day'))
		return when.format(this.SHORT_TIMESTAMP_FORMAT); // Timestamp is for today, ignore the date
	else
		return when.format(this.LONG_TIMESTAMP_FORMAT); // Timestamp is not today, include the date
}

LogUI.prototype.addLine = function (event) {
	var me = this;

	if (this.EVENT_FILTER(event)) {
		var tr = $('<tr/>').addClass('logline');

		tr.append($('<td/>').addClass('log_endpoint').text(this.ENDPOINT_RENDERER(event)));
		tr.append($('<td/>').addClass('log_when').text(this.formatTimestamp(moment(event.when))));
		tr.append($('<td/>').addClass('log_level').text(this.LEVEL_NAMES[event.level]));
		tr.append(this.MESSAGE_RENDERER(event, $('<td/>').addClass('log_message')));

		tr.data('e', event);

		// Keep track of the last time we received a mousedown event.
		// When we receive a click we'll need to make sure that the
		// corresponding mousedown event was <= 150ms ago, this
		// ensures that highlighting text in the cell doesn't cause
		// the cell to expand/contract
		tr.mousedown(function () {
			me.lastTableRowMousedown = new Date();
		});

		tr.click(function () {
			if (me.lastTableRowMousedown) {
				var prev = me.lastTableRowMousedown.getTime();
				var now = new Date().getTime();

				if (now - prev <= 150) {
					me.toggleEventExpansion(this);
				}
			}
		});

		tr.insertBefore(this.END_OF_LOG_TABLE);

		// If this is a new endpoint make sure we keep track of it
		if (!(event.endpoint in this.ALL_ENDPOINTS)) {
			this.addEndpoint(event.endpoint);
		}

		// Recognise when a previously-http://unknown instance id gets an endpoint.
		// When this happens, add an entry in a fixup array of instance id -> endpoint and rerender()
		if (event.endpoint == 'http://unknown') {
			this.OVERRIDE_ENDPOINTS[event.instanceId] = event.endpoint;
		}
		else if (this.OVERRIDE_ENDPOINTS[event.instanceId] == 'http://unknown') {
			this.OVERRIDE_ENDPOINTS[event.instanceId] = event.endpoint;

			this.rerender(
				function (e) {
					return (e.endpoint == 'http://unknown');
				},
				function (e) {
					if (e.endpoint == 'http://unknown' && e.instanceId in me.OVERRIDE_ENDPOINTS) {
						e.endpoint = me.OVERRIDE_ENDPOINTS[e.instanceId];
						return e;
					}
					else {
						return null;
					}
				});
		}
	}
};

LogUI.prototype.toggleEventExpansion = function (clickedElement) {
	var tr = $(clickedElement).closest('tr.logline');
	var event = tr.data('e');
	var expanded = tr.data('expanded');

	if (!event) {
		throw "Could not read event from log line!";
	}

	if (expanded == true) {
		tr.data('expanded', null);

		this.MESSAGE_RENDERER(event, tr.find('td.log_message'));
	}
	else {
		tr.data('expanded', true);

		var td = tr.find('td.log_message');
		td.empty();

		var msg = $('<pre />');
		msg.text(event.message);
		td.append(msg);

		td.append($('<br />'));

		td.append($('<div/>').text('Endpoint: ' + event.endpoint));
		td.append($('<div/>').text('Instance ID: ' + event.instanceId));
		td.append($('<div/>').text('Logger: ' + event.category));
		td.append($('<div/>').text('Trace ID: ' + event.traceId));
		td.append($('<div/>').text('User ID: ' + event.userId));
		td.append($('<div/>').text('Thread ID: ' + event.threadId));

		if (event.exceptionId != null && event.exceptionId.length > 0) {
			var exceptionId = $('<div/>').text('Exception ID: ' + event.exceptionId);

			// TODO make exceptionId a link to search logs for similar exceptions?

			td.append(exceptionId);

			var ex = $('<pre/>')
			ex.text(event.exception);

			td.append(ex);
		}

	}
};

LogUI.prototype.addEndpoint = function (endpoint) {
	var me = this;

	this.ALL_ENDPOINTS[endpoint] = true;

	var previouslyChecked = this.getFilteredEndpoints();

	var dropdown = this.uiroot.find('#filter-endpoint');
	var options = dropdown.find('.menu');

	// Remove the existing items
	options.find("div.item").remove();

	var keys = Object.keys(this.ALL_ENDPOINTS).sort();

	for (var i = 0; i < keys.length; i++) {
		var url = keys[i];
		var entry = $('<div class="item" data-inverted="" data-position="right center" />');

		entry.attr('data-tooltip', url);
		entry.attr('data-value', url);
		entry.text(this.ENDPOINT_RENDERER({'endpoint': url}));

		options.append(entry);
	}

	dropdown.dropdown('refresh');

	// TODO set filtered endpoints
}

// eventSelector is optional, if it is specified then it should return true for lines which should be rerendered
// eventMutator is optional, if it returns a non-null value then that return value is saved as the event data
LogUI.prototype.rerender = function (eventSelector, eventMutator) {
	var me = this;

	// Now rerender the lines
	// N.B. we currently only rerender the timestamp and endpoint
	this.uiroot.find("tr.logline").each(function () {
		var tr = $(this);
		var event = tr.data('e');

		if (eventSelector === undefined || eventSelector(event)) {
			// Allow the event mutator to modify events
			if (eventMutator !== undefined) {
				event = eventMutator(event);

				if (event)
					tr.data('e', event);
			}

			var endpoint = tr.find("td.log_endpoint");
			endpoint.text(me.ENDPOINT_RENDERER(event));

			var timestamp = tr.find("td.log_when");
			timestamp.text(me.formatTimestamp(moment(event.when)));
		}
	});
};

LogUI.prototype.refilter = function () {
	var permittedEndpoints = this.getFilteredEndpoints();

	var minLevel = this.uiroot.find("#filter-level").dropdown('get value');

	if (minLevel == "")
		minLevel = 0;

	// Set up a filter function
	// If no filtering is requested use a function that always returns true
	if (permittedEndpoints.length == 0 && minLevel <= 2)
		this.setEventFilter(function () {
			return true;
		});
	else
		this.setEventFilter(function (event) {
			return (permittedEndpoints.length == 0 || permittedEndpoints.includes(event.endpoint)) && event.level >= minLevel;
		});
};

LogUI.prototype.setEventFilter = function (newFilterFunction) {
	var me = this;
	this.EVENT_FILTER = newFilterFunction;

	// Pick up all the log lines
	var rows = this.uiroot.find('#loglines tr').slice(0, -1);

	// First, show all rows
	rows.show();

	// Now hide ineligible rows
	rows.each(function () {
		var row = $(this);
		var event = row.data('e');

		if (!me.EVENT_FILTER(event))
			row.hide();
	});
};

LogUI.prototype.getFilteredEndpoints = function () {
	return this.uiroot.find("#filter-endpoint").dropdown('get values');
};

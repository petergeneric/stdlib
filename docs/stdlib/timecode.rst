Helper Types for Media Processing
=================================

This library contains useful primitives for working with media samples

Timebase
--------

Encodes the number of frames/samples of some media that cover one second. Represented as a fraction with a numerator and denominator.
European and web systems typically use integer timebases for video (e.g. 25 or 30), whereas American broadcast systems often use fractional (e.g. 29.97, represented as 30000/1001) for legacy reasons.
Timebase is also used for audio, where the values are significantly larger then video - 48,000 is typical

SampleCount
-----------

A number of samples at a given timebase; this concept allows us to represent a media duration in a manner that can be easily converted between timebases (or to real time). For instance, it is common to convert a video SampleCount into the equivalent audio SampleCount that would cover the same period of time.

Timecode
--------

This is a more structured representation than SampleCount, and is expressed in days (often zero and omitted), hours, minutes, seconds and frames (where frames is the number of samples modulo the timebase).
Timecodes are labels applied to samples: while in non-drop-frame timecode mode (generally anything but NTSC) it's a simple splitting of a sample count, in drop-frame mode certain timecode values are "skipped" (this is the "drop" in "drop frame"). See `Wikipedia's SMPTE Timecode page <https://en.wikipedia.org/wiki/SMPTE_timecode#Drop_frame_timecode>`_ for further reading on how drop-frame timecode works

The Timecode class represents non-drop-frame and drop-frame timecodes (i.e. PAL and NTSC), which are standard ways to label frames within some media file.
This class supports large timebases (e.g. for audio)

A Timecode instance can be:

 - Converted into a different Timebase and/or drop-frame flag
 - Converted to a ``SampleCount`` offset from some other timecode (e.g. ``00:00:00:00``)
 - Be offset by adding or subtracting a ``SampleCount``
 - Converted to other common string representations (SMPTE, SMPTE with timebase, FFmpeg)

TimecodeRange
-------------

This helper class allows tests to be performed on an inclusive range of timecodes. It is constructed either with a ``start Timecode`` and an ``end Timecode`` or with a ``start Timecode`` and a ``duration SampleCount``. Generally it's used to determine whether a timecode is within a range of media (or whether 2 TimecodeRanges overlap or are contained within one another)

TimecodeBuilder
---------------

This is a builder class that can be used to build up a Timecode instance from raw field values or wallclock times.

Example usage:

.. code-block:: java

	return new TimecodeBuilder()
	                       .withDays(0)
	                       .withHours(23)
	                       .withMinutes(59)
	                       .withSeconds(59)
	                       .withFrames(23)
	                       .withDropFrame(false)
	                       .withRate(Timebase.HZ_25);



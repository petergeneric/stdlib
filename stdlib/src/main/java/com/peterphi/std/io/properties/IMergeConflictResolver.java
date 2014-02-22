package com.peterphi.std.io.properties;

public interface IMergeConflictResolver
{
	public String resolveConflict(String varName, String currentValue, String valueToBeMerged);
}

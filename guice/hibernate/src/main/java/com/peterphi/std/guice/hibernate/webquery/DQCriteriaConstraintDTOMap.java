package com.peterphi.std.guice.hibernate.webquery;

import java.util.HashMap;
import java.util.Map;

/**
 * A DTO that is to be built up so that a users Query params can be translated along this chain:
 * Query Params -> ResultSetConstraints -> DQCriteriaConstraintDTO -> DQuery -> Criteria
 */
public class DQCriteriaConstraintDTOMap
{
	private Map<RestrictionFunctionType, HashMap<String, DQCriteriaConstraintDTO>> criteriaConstraintDTOMap = new HashMap<RestrictionFunctionType, HashMap<String, DQCriteriaConstraintDTO>>(11);

	public HashMap<String, DQCriteriaConstraintDTO> getEntries(RestrictionFunctionType type)
	{
		if (criteriaConstraintDTOMap.containsKey(type))
		{
			return criteriaConstraintDTOMap.get(type);
		}
		return null;
	}

	public void put(RestrictionFunctionType type, String field, String value)
	{
		if (criteriaConstraintDTOMap.containsKey(type))
		{
			DQCriteriaConstraintDTO item = criteriaConstraintDTOMap.get(type).get(field);
			if (item != null)
				item.values.add(value);
			else
			{
				criteriaConstraintDTOMap.get(type).put(field, createDQCriteriaConstraintDTO(field, value));
			}
		}
		else
		{
			HashMap<String, DQCriteriaConstraintDTO> constraints = new HashMap<String, DQCriteriaConstraintDTO>(11);
			constraints.put(field, createDQCriteriaConstraintDTO(field, value));
			criteriaConstraintDTOMap.put(type, constraints);
		}
	}

	private DQCriteriaConstraintDTO createDQCriteriaConstraintDTO(String field, String value)
	{
		DQCriteriaConstraintDTO dqCriteriaConstraintDTO = new DQCriteriaConstraintDTO();
		dqCriteriaConstraintDTO.field = field;
		dqCriteriaConstraintDTO.values.add(value);
		return dqCriteriaConstraintDTO;
	}
}

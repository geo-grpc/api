package com.esri.core.geometry;


import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleStringCursor extends StringCursor {
	private ArrayDeque<String> m_arrayDeque;
	private ArrayDeque<SimpleStateEnum> m_simpleStates;
	private ArrayDeque<Integer> m_ids;
	private ArrayDeque<String> m_featureIDs;
	private String m_currentFeatureID = "";
	private int m_current_id = -1;
	private SimpleStateEnum m_current_state = SimpleStateEnum.SIMPLE_UNKNOWN;
	private Envelope2D m_env2D = new Envelope2D();

	@Deprecated
	public SimpleStringCursor(String inputString) {
		m_arrayDeque = new ArrayDeque<>(1);
		m_arrayDeque.push(inputString);
	}

	public SimpleStringCursor(String inputString, int id) {
		m_arrayDeque = new ArrayDeque<>(1);
		m_arrayDeque.push(inputString);
		m_ids = new ArrayDeque<>(1);
		m_ids.push(id);
	}

	public SimpleStringCursor(String inputString, int id, SimpleStateEnum simpleState) {
		m_arrayDeque = new ArrayDeque<>(1);
		m_arrayDeque.push(inputString);
		m_ids = new ArrayDeque<>(1);
		m_ids.push(id);
		m_simpleStates = new ArrayDeque<>(1);
		m_simpleStates.push(simpleState);
	}

	public SimpleStringCursor(String inputString, int id, SimpleStateEnum simpleState, String featureID) {
		m_arrayDeque = new ArrayDeque<>(1);
		m_arrayDeque.push(inputString);
		m_ids = new ArrayDeque<>(1);
		m_ids.push(id);
		m_simpleStates = new ArrayDeque<>(1);
		m_simpleStates.push(simpleState);
		m_featureIDs = new ArrayDeque<>(1);
		m_featureIDs.push(featureID);
	}

	@Deprecated
	public SimpleStringCursor(String[] inputStringArray) {
		m_arrayDeque = Arrays.stream(inputStringArray).collect(Collectors.toCollection(ArrayDeque::new));
	}

	@Deprecated
	public SimpleStringCursor(List<String> inputStringArray) {
		m_arrayDeque = new ArrayDeque<>(inputStringArray);
	}

	public SimpleStringCursor(ArrayDeque<String> arrayDeque, ArrayDeque<Integer> ids) {
		m_ids = ids;
		m_arrayDeque = arrayDeque;
	}

	public SimpleStringCursor(ArrayDeque<String> arrayDeque, ArrayDeque<Integer> ids, ArrayDeque<SimpleStateEnum> simpleStates) {
		m_ids = ids;
		m_arrayDeque = arrayDeque;
		m_simpleStates = simpleStates;
	}

	public SimpleStringCursor(ArrayDeque<String> arrayDeque,
	                          ArrayDeque<Integer> ids,
	                          ArrayDeque<SimpleStateEnum> simpleStates,
	                          ArrayDeque<String> featureIDs) {
		if ((arrayDeque.size() & ids.size() & simpleStates.size() & featureIDs.size()) != arrayDeque.size()) {
			throw new GeometryException("arrays must be same size");
		}
		m_arrayDeque = arrayDeque;
		m_ids = ids;
		m_simpleStates = simpleStates;
		m_featureIDs = featureIDs;
	}

	@Override
	public int getID() {
		return m_current_id;
	}

	@Override
	public String getFeatureID() {
		return m_currentFeatureID;
	}

	@Override
	public SimpleStateEnum getSimpleState() {
		return m_current_state;
	}

	@Override
	public Envelope2D getEnvelope2D() {
		return m_env2D;
	}

	@Override
	public boolean hasNext() {
		return m_arrayDeque.size() > 0;
	}

	void _incrementInternals() {
		if (m_ids != null && !m_ids.isEmpty()) {
			m_current_id = m_ids.pop();
		} else {
			m_current_id++;
		}

		if (m_simpleStates != null && !m_simpleStates.isEmpty()) {
			m_current_state = m_simpleStates.pop();
		}
		if (m_featureIDs != null && !m_featureIDs.isEmpty()) {
			m_currentFeatureID = m_featureIDs.pop();
		}
	}

	public String next() {
		if (hasNext()) {
			_incrementInternals();
			return m_arrayDeque.pop();
		}

		return null;
	}
}

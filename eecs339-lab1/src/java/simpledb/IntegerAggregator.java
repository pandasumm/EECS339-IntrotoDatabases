package simpledb;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Knows how to compute some aggregate over a set of IntFields.
 */
public class IntegerAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;
    private int gbfield;
    private Type gbfieldtype;
    private int afield;
    private Op op;
    private HashMap<Field, Integer> groups;
    private HashMap<Field, Integer> groupCount;
    /**
     * Aggregate constructor
     * 
     * @param gbfield
     *            the 0-based index of the group-by field in the tuple, or
     *            NO_GROUPING if there is no grouping
     * @param gbfieldtype
     *            the type of the group by field (e.g., Type.INT_TYPE), or null
     *            if there is no grouping
     * @param afield
     *            the 0-based index of the aggregate field in the tuple
     * @param what
     *            the aggregation operator
     */

    public IntegerAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // some code goes here
    	this.gbfield = gbfield;
    	this.gbfieldtype = gbfieldtype;
    	this.afield = afield;
    	this.op = what;
    	this.groups = new HashMap<Field, Integer>();
    	this.groupCount = new HashMap<Field, Integer>();
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the
     * constructor
     * 
     * @param tup
     *            the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
    	
        // some code goes here
    	Field gbField;
    	IntField aField;
    	int aValue;
    	int currentValue, currentCount;
    	gbField = this.gbfield == Aggregator.NO_GROUPING ? new IntField(Aggregator.NO_GROUPING) : (Field)tup.getField(this.gbfield);
    	aField = (IntField)tup.getField(this.afield);
    	aValue = aField.getValue();
    	if(!groups.containsKey(gbField)) {
    		groups.put(gbField, aValue);
    		groupCount.put(gbField, 1);
    	}
    	else {
    		currentValue = groups.get(gbField);
    		currentCount = groupCount.get(gbField);
    		groupCount.put(gbField, currentCount+1);
    		switch (this.op) {
    		case SUM: case AVG:{
    			groups.put(gbField, currentValue + aValue);
    			break;
    		}
    		case MIN: {
    			if(aValue < currentValue) {
    				groups.put(gbField, aValue);
    			}
    			break;
    		}
    		case MAX: {
    			if(aValue > currentValue) {
    				groups.put(gbField, aValue);
    			}
    			break;
    		}
    		default:{
    			groups.put(gbField, aValue);
    			break;
    		}
    		}
    	}

    }

    /**
     * Create a OpIterator over group aggregate results.
     * 
     * @return a OpIterator whose tuples are the pair (groupVal, aggregateVal)
     *         if using group, or a single (aggregateVal) if no grouping. The
     *         aggregateVal is determined by the type of aggregate specified in
     *         the constructor.
     */
    public OpIterator iterator() {
        // some code goes here
        // throw new
        // UnsupportedOperationException("please imment me for lab2");
    	ArrayList<Tuple> tuples = new ArrayList<Tuple>();
    	Tuple gbTuple;
    	if(this.gbfield == Aggregator.NO_GROUPING) {
    		Type[] tdType = {Type.INT_TYPE};
    		TupleDesc gbTupleDesc = new TupleDesc(tdType);
    		gbTuple = new Tuple(gbTupleDesc);
    		Field gbfield = new IntField(Aggregator.NO_GROUPING);
    		switch(this.op) {
    		case AVG: 
    			gbTuple.setField(0, new IntField(groups.get(gbfield)/groupCount.get(gbfield)));
    			break;
    		case SUM: case MIN: case MAX: 
    			gbTuple.setField(0, new IntField(groups.get(gbfield)));
    			break;
    		case COUNT:
    			gbTuple.setField(0, new IntField(groupCount.get(gbfield)));
    			break;
    		}
    		tuples.add(gbTuple);
    		return new TupleIterator(gbTupleDesc, tuples);
    	}
    	else {
	    	Type[] tdType = {this.gbfieldtype, Type.INT_TYPE};
	    	TupleDesc gbTupleDesc = new TupleDesc(tdType);
	    	for(Field k : groups.keySet()) {   
	    		gbTuple = new Tuple(gbTupleDesc);
	    		gbTuple.setField(0, k);
	    		switch(this.op) {
	    		case AVG: {
	    			gbTuple.setField(1, new IntField(groups.get(k)/groupCount.get(k)));
	    			break;
	    		}
	    		case SUM: case MIN: case MAX: {
	    			gbTuple.setField(1, new IntField(groups.get(k)));
	    			break;
	    		}
	    		case COUNT: {
	    			gbTuple.setField(1, new IntField(groupCount.get(k)));
	    			break;
	    		}
	    		}
	    		tuples.add(gbTuple);
	    	}
	    	return new TupleIterator(gbTupleDesc, tuples);
    	}
    }

}

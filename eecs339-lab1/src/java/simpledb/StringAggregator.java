package simpledb;

import java.util.ArrayList;
import java.util.HashMap;

import simpledb.Aggregator.Op;

/**
 * Knows how to compute some aggregate over a set of StringFields.
 */
public class StringAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;
    private int gbfield;
    private Type gbfieldtype;
    private int afield;
    private Op op;
    //private HashMap<Field, Integer> groups;
    private HashMap<Field, Integer> groupCount;

    /**
     * Aggregate constructor
     * @param gbfield the 0-based index of the group-by field in the tuple, or NO_GROUPING if there is no grouping
     * @param gbfieldtype the type of the group by field (e.g., Type.INT_TYPE), or null if there is no grouping
     * @param afield the 0-based index of the aggregate field in the tuple
     * @param what aggregation operator to use -- only supports COUNT
     * @throws IllegalArgumentException if what != COUNT
     */

    public StringAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // some code goes here
    	this.gbfield = gbfield;
    	this.gbfieldtype = gbfieldtype;
    	this.afield = afield;
    	this.op = what;
    	//this.groups = new HashMap<Field, Integer>();
    	this.groupCount = new HashMap<Field, Integer>();
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the constructor
     * @param tup the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here
    	Field gbField, aField;
    	int gbKey, aValue;
    	int currentValue, currentCount;
    	gbField = tup.getField(this.gbfield);
    	//aField = tup.getField(this.afield);
    	//aValue = aField.getValue();
    	if(!groupCount.containsKey(gbField)) {
    		//groups.put(gbField, aValue);
    		groupCount.put(gbField, 1);
    	}
    	else {
    		currentCount = groupCount.get(gbField);
    		groupCount.put(gbField, currentCount+1);
    	}
    }

    /**
     * Create a OpIterator over group aggregate results.
     *
     * @return a OpIterator whose tuples are the pair (groupVal,
     *   aggregateVal) if using group, or a single (aggregateVal) if no
     *   grouping. The aggregateVal is determined by the type of
     *   aggregate specified in the constructor.
     */
    public OpIterator iterator() {
        // some code goes here
    	ArrayList<Tuple> tuples = new ArrayList<Tuple>();
    	Tuple gbTuple;
    	if(this.gbfield == Aggregator.NO_GROUPING) {
    		Type[] tdType = {Type.INT_TYPE}; 
        	TupleDesc gbTupleDesc = new TupleDesc(tdType);
    		gbTuple = new Tuple(gbTupleDesc);
    		if(this.op == Op.COUNT) {
    			gbTuple.setField(0, new IntField(groupCount.get(this.gbfield)));
    		}
    		tuples.add(gbTuple);        	
        	return new TupleIterator(gbTupleDesc, tuples);
    	}
    	else {
	    	Type[] tdType = {this.gbfieldtype, Type.INT_TYPE}; 
	    	TupleDesc gbTupleDesc = new TupleDesc(tdType);
	    	for(Field k : groupCount.keySet()) {   
	    		gbTuple = new Tuple(gbTupleDesc);
	    		gbTuple.setField(0, k);
	    		if(this.op == Op.COUNT) {
	    			gbTuple.setField(1, new IntField(groupCount.get(k)));
	    		}
	    		tuples.add(gbTuple);
	    	}
	    	return new TupleIterator(gbTupleDesc, tuples);
    	}
    }

}

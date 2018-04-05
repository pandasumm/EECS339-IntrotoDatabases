package simpledb;

import java.util.*;

/**
 * The Aggregation operator that computes an aggregate (e.g., sum, avg, max,
 * min). Note that we only support aggregates over a single column, grouped by a
 * single column.
 */
public class Aggregate extends Operator {

    private static final long serialVersionUID = 1L;
    private OpIterator child;
    private int afield;
    private int gfield;
    private Aggregator.Op aop;
    private Aggregator aggregator;
    private OpIterator iterator;
    private OpIterator[] children;
    /**
     * Constructor.
     * 
     * Implementation hint: depending on the type of afield, you will want to
     * construct an {@link IntegerAggregator} or {@link StringAggregator} to help
     * you with your implementation of readNext().
     * 
     * 
     * @param child
     *            The OpIterator that is feeding us tuples.
     * @param afield
     *            The column over which we are computing an aggregate.
     * @param gfield
     *            The column over which we are grouping the result, or -1 if
     *            there is no grouping
     * @param aop
     *            The aggregation operator to use
     */
    public Aggregate(OpIterator child, int afield, int gfield, Aggregator.Op aop) {
	// some code goes here
    	this.child = child;
    	this.afield = afield;
    	this.gfield = gfield;
    	this.aop = aop;
    	
    	Type gFieldType;
    	Type aFieldType = child.getTupleDesc().getFieldType(this.afield);
    	if (gfield==Aggregator.NO_GROUPING) {
    		gFieldType = null;
    	} else {
    		gFieldType = child.getTupleDesc().getFieldType(gfield);
    	}
    	
    	if (aFieldType == Type.INT_TYPE) {
    		aggregator = new IntegerAggregator(gfield, gFieldType, afield, aop);
    	} else {
    		aggregator = new StringAggregator(gfield, gFieldType, afield, aop);
    	}
    }

    /**
     * @return If this aggregate is accompanied by a groupby, return the groupby
     *         field index in the <b>INPUT</b> tuples. If not, return
     *         {@link simpledb.Aggregator#NO_GROUPING}
     * */
    public int groupField() {
	// some code goes here
    	return this.gfield;
    	//return -1;
    }

    /**
     * @return If this aggregate is accompanied by a group by, return the name
     *         of the groupby field in the <b>OUTPUT</b> tuples. If not, return
     *         null;
     * */
    public String groupFieldName() {
	// some code goes here
    	if(this.gfield == simpledb.Aggregator.NO_GROUPING)
    		return null;
    	return this.child.getTupleDesc().getFieldName(gfield);
    	//return null;
    }

    /**
     * @return the aggregate field
     * */
    public int aggregateField() {
	// some code goes here
		return this.afield;
    }

    /**
     * @return return the name of the aggregate field in the <b>OUTPUT</b>
     *         tuples
     * */
    public String aggregateFieldName() {
	// some code goes here
		return this.child.getTupleDesc().getFieldName(afield);
    }

    /**
     * @return return the aggregate operator
     * */
    public Aggregator.Op aggregateOp() {
	// some code goes here
    	return this.aop;
    }

    public static String nameOfAggregatorOp(Aggregator.Op aop) {
    	return aop.toString();
    }

    public void open() throws NoSuchElementException, DbException,
	    TransactionAbortedException {
	// some code goes here
    	this.child.open();
    	super.open();
    	while(this.child.hasNext()) {
    		this.aggregator.mergeTupleIntoGroup(this.child.next());
    	}
    	this.iterator = this.aggregator.iterator();
    	this.iterator.open();
    }

    /**
     * Returns the next tuple. If there is a group by field, then the first
     * field is the field by which we are grouping, and the second field is the
     * result of computing the aggregate. If there is no group by field, then
     * the result tuple should contain one field representing the result of the
     * aggregate. Should return null if there are no more tuples.
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
	// some code goes here
		if(this.iterator.hasNext())
			return this.iterator.next();
		return null;
    }

    public void rewind() throws DbException, TransactionAbortedException {
	// some code goes here
    	this.child.rewind();
    	this.iterator.rewind();
    }

    /**
     * Returns the TupleDesc of this Aggregate. If there is no group by field,
     * this will have one field - the aggregate column. If there is a group by
     * field, the first field will be the group by field, and the second will be
     * the aggregate value column.
     * 
     * The name of an aggregate column should be informative. For example:
     * "aggName(aop) (child_td.getFieldName(afield))" where aop and afield are
     * given in the constructor, and child_td is the TupleDesc of the child
     * iterator.
     */
    public TupleDesc getTupleDesc() {
	// some code goes here
    	Type[] fieldType;
        String[] fieldName;
    	if(this.gfield == Aggregator.NO_GROUPING) {
    		fieldType = new Type[1];
            fieldName = new String[1];
            fieldType[0] = this.child.getTupleDesc().getFieldType(afield);
            fieldName[0] = this.aop.toString() + "(" + child.getTupleDesc().getFieldName(afield) + ")";
            return new TupleDesc(fieldType, fieldName);
    	}
    	else{
    		fieldType = new Type[2];
            fieldName = new String[2];
            fieldType[0] = this.child.getTupleDesc().getFieldType(gfield);
            fieldName[0] = this.child.getTupleDesc().getFieldName(gfield);
            fieldType[1] = this.child.getTupleDesc().getFieldType(afield);
            fieldName[1] = this.aop.toString() + "(" + child.getTupleDesc().getFieldName(afield) + ")";
            return new TupleDesc(fieldType, fieldName);
    	}
    }

    public void close() {
	// some code goes here
    	this.iterator.close();
    	this.child.close();
    	super.close();
    }

    @Override
    public OpIterator[] getChildren() {
	// some code goes here
    	return this.children;
    }

    @Override
    public void setChildren(OpIterator[] children) {
	// some code goes here
    	this.children = children;
    }
    
}

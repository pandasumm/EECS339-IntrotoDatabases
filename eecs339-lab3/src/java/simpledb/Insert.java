package simpledb;

import java.io.IOException;

/**
 * Inserts tuples read from the child operator into the tableId specified in the
 * constructor
 */
public class Insert extends Operator {

    private static final long serialVersionUID = 1L;
    private TransactionId tid;
    private OpIterator child;
    private int tableid;
    private boolean fetched = false;
    private TupleDesc td;
    //private OpIterator[] children;
    
    /**
     * Constructor.
     *
     * @param t
     *            The transaction running the insert.
     * @param child
     *            The child operator from which to read tuples to be inserted.
     * @param tableId
     *            The table in which to insert tuples.
     * @throws DbException
     *             if TupleDesc of child differs from table into which we are to
     *             insert.
     */
    public Insert(TransactionId t, OpIterator child, int tableId)
            throws DbException {
        // some code goes here
    	this.tid = t;
    	this.child = child;
    	this.tableid = tableId;
    	Type[] tdType = new Type[]{Type.INT_TYPE};
    	String[] tdStr = new String[]{"Numbers of tuple inserted"};
    	this.td = new TupleDesc(tdType, tdStr); 
    }

    public TupleDesc getTupleDesc() {
        // some code goes here
        return this.td;
    }

    public void open() throws DbException, TransactionAbortedException {
        // some code goes here
    	this.child.open();
    	super.open();
    }

    public void close() {
        // some code goes here
    	this.child.close();
    	super.close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // some code goes here
    	this.child.rewind();
    }

    /**
     * Inserts tuples read from child into the tableId specified by the
     * constructor. It returns a one field tuple containing the number of
     * inserted records. Inserts should be passed through BufferPool. An
     * instances of BufferPool is available via Database.getBufferPool(). Note
     * that insert DOES NOT need check to see if a particular tuple is a
     * duplicate before inserting it.
     *
     * @return A 1-field tuple containing the number of inserted records, or
     *         null if called more than once.
     * @see Database#getBufferPool
     * @see BufferPool#insertTuple
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // some code goes here
    	Tuple tup= new Tuple(td);
        try{
            if (fetched){
                return null;
            } else {
            	int count = 0;
                fetched=true;
                while (child.hasNext()){
                    try {    
                    Database.getBufferPool().insertTuple(this.tid, this.tableid, child.next());
                    }
                    catch (IOException e){
                            e.printStackTrace();
                    }
                    count++;
                }
            	tup.setField(0, new IntField(count));
            }
        }
        catch (DbException e){
                e.printStackTrace();
            }
        catch (TransactionAbortedException e){
                e.printStackTrace();
            }
        System.out.println("insert test: "+tup.toString());
        return tup;
    }

    @Override
    public OpIterator[] getChildren() {
        // some code goes here
        return new OpIterator[] {this.child};
    }

    @Override
    public void setChildren(OpIterator[] children) {
        // some code goes here
    	this.child = children[0];
    }
}

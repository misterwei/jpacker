package jpacker.local;


/**
 * @author cool
 *
 */
public class SelectListContext<T> extends SelectContext<T>{
	private int start = -1;             
	private int rows = -1;             
	private boolean isLimitQuery = false;
	
	/**
	 * page query
	 * 
	 * @param standardSql
	 * @param parameters
	 * @param start
	 * @param end
	 */
	public SelectListContext(Class<T> returnClass,String selectSql,int start,int rows,Object[] parameters){
		super(returnClass,selectSql,parameters);
		this.start = start;
		this.rows = rows;
		if(start != -1 && rows != -1){
			this.isLimitQuery = true;
		}
	}
	
	public SelectListContext(Class<T> returnClass,String selectSql,Object[] parameters){
		super(returnClass,selectSql,parameters);
	}
	

	public int getOffset() {
		return start;
	}

	public int getLimit() {
		return rows;
	}

	public boolean isLimitQuery(){
		return isLimitQuery;
	}
	
}

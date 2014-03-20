/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jpacker.processor;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jpacker.connection.ConnectionHolder;
import jpacker.factory.TableFactory;
import jpacker.local.LocalExecutor;
import jpacker.local.SelectContext;
import jpacker.local.SelectListContext;
import jpacker.model.ColumnModel;
import jpacker.model.SelectModel;
import jpacker.model.SimpleProperty;
import jpacker.model.TableModel;
import jpacker.proxy.ProxyFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * <code>BeanProcessor</code> matches column names to bean property names 
 * and converts <code>ResultSet</code> columns into objects for those bean 
 * properties.  Subclasses should override the methods in the processing chain
 * to customize behavior.
 * </p>
 * 
 * <p>
 * This class is thread-safe.
 * </p>
 * 
 * @see BasicRowProcessor
 * 
 * @since DbUtils 1.1
 */
public class TableProcessor {
	static Logger log = LoggerFactory.getLogger(TableProcessor.class);
	
    /**
     * Special array value used by <code>mapColumnsToProperties</code> that 
     * indicates there is no bean property that matches a column from a 
     * <code>ResultSet</code>.
     */
    protected static final int PROPERTY_NOT_FOUND = -1;

    /**
     * Set a bean's primitive properties to these defaults when SQL NULL 
     * is returned.  These are the same as the defaults that ResultSet get* 
     * methods return in the event of a NULL column.
     */
    private static final Map<Class<?>, Object> primitiveDefaults = new HashMap<Class<?>, Object>();

    static {
        primitiveDefaults.put(Integer.TYPE, 0);
        primitiveDefaults.put(Short.TYPE, (Short)((short) 0));
        primitiveDefaults.put(Byte.TYPE, (Byte)((byte) 0));
        primitiveDefaults.put(Float.TYPE, (Float)(float)(0));
        primitiveDefaults.put(Double.TYPE, (Double)(double)(0));
        primitiveDefaults.put(Long.TYPE, (Long)(0L));
        primitiveDefaults.put(Boolean.TYPE, Boolean.FALSE);
        primitiveDefaults.put(Character.TYPE, '\u0000');
    }

    
    private TableFactory tableContext;
    
    private LocalExecutor localExecutor;
    
    private ProxyFactory proxyFactory;
    
    /**
     * Constructor for BeanProcessor.
     */
    public TableProcessor(TableFactory tableContext,LocalExecutor localExecutor,ProxyFactory proxyFactory) {
        this.tableContext = tableContext;
        this.localExecutor = localExecutor;
        this.proxyFactory = proxyFactory;
    }

    public TableFactory getTableFactory(){
    	return tableContext;
    }
    
    /**
     * Convert a <code>ResultSet</code> row into a JavaBean.  This 
     * implementation uses reflection and <code>BeanInfo</code> classes to 
     * match column names to bean property names.  Properties are matched to 
     * columns based on several factors:
     * <br/>
     * <ol>
     *     <li>
     *     The class has a writable property with the same name as a column.
     *     The name comparison is case insensitive.
     *     </li>
     * 
     *     <li>
     *     The column type can be converted to the property's set method 
     *     parameter type with a ResultSet.get* method.  If the conversion fails
     *     (ie. the property was an int and the column was a Timestamp) an
     *     SQLException is thrown.
     *     </li>
     * </ol>
     * 
     * <p>
     * Primitive bean properties are set to their defaults when SQL NULL is
     * returned from the <code>ResultSet</code>.  Numeric fields are set to 0
     * and booleans are set to false.  Object bean properties are set to 
     * <code>null</code> when SQL NULL is returned.  This is the same behavior
     * as the <code>ResultSet</code> get* methods.
     * </p>
     * @param <T> The type of bean to create
     * @param rs ResultSet that supplies the bean data
     * @param type Class from which to create the bean instance
     * @throws SQLException if a database access error occurs
     * @return the newly created bean
     */
    public <T> T toBean(ConnectionHolder conn,ResultSet rs, Class<T> type) throws SQLException {
    	TableModel table = tableContext.get(type);
        ColumnModel[] columnModels = table.getAllColumnModels();
        ResultSetMetaData rsmd = rs.getMetaData();
        int[] columnToProperty = this.mapColumnsToProperties(rsmd, columnModels);
        
        return toBean(conn,rs, type,columnModels,columnToProperty,table.getTimelySelectModels(),table.isLazy());
        
    }
    
    @SuppressWarnings("unchecked")
	public <T> T toBean(ConnectionHolder conn,ResultSet rs, Class<T> type,ColumnModel[] columnModels, int[] columnToProperty,SelectModel[] timelySelects,boolean lazy) throws SQLException {
    	
        T obj = null;
        
        if(lazy){
        	obj = this.createLazyBean(rs, type, columnModels, columnToProperty);
        }else{
        	obj = this.createBean(rs, type, columnModels, columnToProperty);
        }
        
        if(timelySelects != null && timelySelects.length > 0){

        	for(SelectModel s : timelySelects){
				Object[] parameters = null;
				try{
					SimpleProperty[] ps = s.getRefProperties();
					if(ps != null){
						parameters = new Object[ps.length];
						for(int i=0;i<ps.length;i++){
							parameters[i] = ps[i].invokeRead(obj);
						}
					}
					if(s.isReturnList()){
						Object result = localExecutor.selectList(new SelectListContext(s.getTargetType(), s.getSql(), s.getOffset(),s.getLimit(), parameters), conn);
						SimpleProperty p = s.getProperty();
						p.invokeWrite(obj,result);
					}else if(s.isHasRef()){
						Object[] propertyResult = (Object[])localExecutor.selectOne(new SelectContext(Object[].class, s.getSql(), parameters),conn);
						for(int i=0;i<propertyResult.length;i++){
							SimpleProperty p = s.getProperty(i+1);
							p.invokeWrite(obj,propertyResult[i]);
						}
					}else{
						Object result = localExecutor.selectOne(new SelectContext(s.getTargetType(), s.getSql(), parameters),conn);
						SimpleProperty p = s.getProperty();
						p.invokeWrite(obj,result);
					}
					
				}catch(Exception e){
					throw new SQLException(e);
				}
			}
			
        }
        
        return obj;
        
    }
    
    
    /**
     * Convert a <code>ResultSet</code> into a <code>List</code> of JavaBeans.  
     * This implementation uses reflection and <code>BeanInfo</code> classes to 
     * match column names to bean property names. Properties are matched to 
     * columns based on several factors:
     * <br/>
     * <ol>
     *     <li>
     *     The class has a writable property with the same name as a column.
     *     The name comparison is case insensitive.
     *     </li>
     * 
     *     <li>
     *     The column type can be converted to the property's set method 
     *     parameter type with a ResultSet.get* method.  If the conversion fails
     *     (ie. the property was an int and the column was a Timestamp) an
     *     SQLException is thrown.
     *     </li>
     * </ol>
     * 
     * <p>
     * Primitive bean properties are set to their defaults when SQL NULL is
     * returned from the <code>ResultSet</code>.  Numeric fields are set to 0
     * and booleans are set to false.  Object bean properties are set to 
     * <code>null</code> when SQL NULL is returned.  This is the same behavior
     * as the <code>ResultSet</code> get* methods.
     * </p>
     * @param <T> The type of bean to create
     * @param rs ResultSet that supplies the bean data
     * @param type Class from which to create the bean instance
     * @throws SQLException if a database access error occurs
     * @return the newly created List of beans
     */
    public <T> List<T> toBeanList(ConnectionHolder connholder,ResultSet rs, Class<T> type) throws SQLException {
        List<T> results = new ArrayList<T>();
        if (!rs.next()) {
            return results;
        }
        

        TableModel table = tableContext.get(type);
        
        ColumnModel[] columnModels = table.getAllColumnModels();
        SelectModel[] timelyselects = table.getTimelySelectModels();
        
        ResultSetMetaData rsmd = rs.getMetaData();
        int[] columnToProperty = this.mapColumnsToProperties(rsmd, columnModels);
        
        do {
            results.add(toBean(connholder,rs, type,columnModels,columnToProperty,timelyselects,table.isLazy()));
        } while (rs.next());

        return results;
    }

    
    /**
     * Creates a new object and initializes its fields from the ResultSet.
     * @param <T> The type of bean to create
     * @param rs The result set.
     * @param type The bean type (the return type of the object).
     * @param props The property descriptors.
     * @param columnToProperty The column indices in the result set.
     * @return An initialized object.
     * @throws SQLException if a database error occurs.
     */
    private <T> T createBean(ResultSet rs, Class<T> type,ColumnModel[] columnModels,int[] columnToProperty)
            throws SQLException {

        T bean = this.newInstance(type);

        for (int i = 0; i < columnToProperty.length; i++) {
        	
        	if(columnToProperty[i] != -1){
	        	ColumnModel columnModel = columnModels[columnToProperty[i]];
	        	Class<?> propType = columnModel.getPropertyType();
        	
	            Object value = this.processColumn(rs, i, propType);
	
	            if (propType != null && value == null && propType.isPrimitive()) {
	                value = primitiveDefaults.get(propType);
	            }
	
	            this.callSetter(bean, columnModel.getProperty(), value);
        	}
        }

        return bean;
    }

    
    private <T> T createLazyBean(ResultSet rs, Class<T> type,ColumnModel[] columnModels,int[] columnToProperty)
            throws SQLException {

        T bean = proxyFactory.getProxyObject(type);

        for (int i = 0; i < columnToProperty.length; i++) {
        	
        	if(columnToProperty[i] != -1){
	        	ColumnModel columnModel = columnModels[columnToProperty[i]];
	        	Class<?> propType = columnModel.getPropertyType();
        	
	            Object value = this.processColumn(rs, i, propType);
	
	            if (propType != null && value == null && propType.isPrimitive()) {
	                value = primitiveDefaults.get(propType);
	            }
	
	            this.callSetter(bean, columnModel.getProperty(), value);
        	}
        }

        return bean;
    }
    
    /**
     * Calls the setter method on the target object for the given property.
     * If no setter method exists for the property, this method does nothing.
     * @param target The object to set the property on.
     * @param prop The property to set.
     * @param value The value to pass into the setter.
     * @throws SQLException if an error occurs setting the property.
     */
    private void callSetter(Object target, SimpleProperty property, Object value)
            throws SQLException {

    	
        Class<?>[] params = property.getWriteParameterTypes();
        try {
            // convert types for some popular ones
            if (value != null) {
                if (value instanceof java.util.Date) {
                    if (params[0].getName().equals("java.sql.Date")) {
                        value = new java.sql.Date(((java.util.Date) value).getTime());
                    } else
                    if (params[0].getName().equals("java.sql.Time")) {
                        value = new java.sql.Time(((java.util.Date) value).getTime());
                    } else
                    if (params[0].getName().equals("java.sql.Timestamp")) {
                        value = new java.sql.Timestamp(((java.util.Date) value).getTime());
                    }
                }
            }

            // Don't call setter if the value object isn't the right type 
            if (this.isCompatibleType(value, params[0])) {
                try {
					property.invokeWrite(target,value);
				} catch (InvocationTargetException e) {
					throw new SQLException(e);
				}
            } else {
              throw new SQLException(
                  "Cannot set " + property.getName() + ": incompatible types.");
            }

        } catch (IllegalArgumentException e) {
            throw new SQLException(
                "Cannot set " + property.getName() + ": " + e.getMessage());

        } 
//        catch (IllegalAccessException e) {
//            throw new SQLException(
//                "Cannot set " + methodName + ": " + e.getMessage());
//
//        } catch (InvocationTargetException e) {
//            throw new SQLException(
//                "Cannot set " + methodName + ": " + e.getMessage());
//        }
    }

    /**
     * ResultSet.getObject() returns an Integer object for an INT column.  The
     * setter method for the property might take an Integer or a primitive int.
     * This method returns true if the value can be successfully passed into
     * the setter method.  Remember, Method.invoke() handles the unwrapping
     * of Integer into an int.
     * 
     * @param value The value to be passed into the setter method.
     * @param type The setter's parameter type.
     * @return boolean True if the value is compatible.
     */
    private boolean isCompatibleType(Object value, Class<?> type) {
        // Do object check first, then primitives
    	//log.info("type {}  value:{}" , new Object[]{type,value});
        if (value == null || type.isInstance(value)) {
            return true;

        } else if (
            type.equals(Integer.TYPE) && Integer.class.isInstance(value)) {
            return true;

        } else if (type.equals(Long.TYPE) && Long.class.isInstance(value)) {
            return true;

        } else if (
            type.equals(Double.TYPE) && Double.class.isInstance(value)) {
            return true;

        } else if (type.equals(Float.TYPE) && Float.class.isInstance(value)) {
            return true;

        } else if (type.equals(Short.TYPE) && Short.class.isInstance(value)) {
            return true;

        } else if (type.equals(Byte.TYPE) && Byte.class.isInstance(value)) {
            return true;

        } else if (
            type.equals(Character.TYPE) && Character.class.isInstance(value)) {
            return true;

        } else if (
            type.equals(Boolean.TYPE) && Boolean.class.isInstance(value)) {
            return true;

        } else {
        	//log.info("type reject {} value:{}" , new Object[]{type,value});
            return false;
        }

    }

    /**
     * Factory method that returns a new instance of the given Class.  This
     * is called at the start of the bean creation process and may be 
     * overridden to provide custom behavior like returning a cached bean
     * instance.
     * @param <T> The type of object to create
     * @param c The Class to create an object from.
     * @return A newly created object of the Class.
     * @throws SQLException if creation failed.
     */
    protected <T> T newInstance(Class<T> c) throws SQLException {
        try {
            return c.newInstance();

        } catch (InstantiationException e) {
            throw new SQLException(
                "Cannot create " + c.getName() + ": " + e.getMessage());

        } catch (IllegalAccessException e) {
            throw new SQLException(
                "Cannot create " + c.getName() + ": " + e.getMessage());
        }
    }

    /**
     * The positions in the returned array represent column numbers.  The 
     * values stored at each position represent the index in the 
     * <code>PropertyDescriptor[]</code> for the bean property that matches 
     * the column name.  If no bean property was found for a column, the 
     * position is set to <code>PROPERTY_NOT_FOUND</code>.
     * 
     * @param rsmd The <code>ResultSetMetaData</code> containing column 
     * information.
     * 
     * @param props The bean property descriptors.
     * 
     * @throws SQLException if a database access error occurs
     *
     * @return An int[] with column index to property index mappings.  The 0th 
     * element is meaningless because JDBC column indexing starts at 1.
     */
    protected int[] mapColumnsToProperties(ResultSetMetaData rsmd,
            ColumnModel[] columns) throws SQLException {

        int cols = rsmd.getColumnCount();
        int columnToProperty[] = new int[cols + 1];
        Arrays.fill(columnToProperty, PROPERTY_NOT_FOUND);

        for (int col = 1; col <= cols; col++) {
            String columnName = rsmd.getColumnLabel(col);
            if (null == columnName || 0 == columnName.length()) {
              columnName = rsmd.getColumnName(col);
            }
            for (int i = 0; i < columns.length; i++) {

                if (columnName.equalsIgnoreCase(columns[i].getName())) {
                    columnToProperty[col] = i;
                    break;
                }
            }
        }

        return columnToProperty;
    }

    /**
     * Convert a <code>ResultSet</code> column into an object.  Simple 
     * implementations could just call <code>rs.getObject(index)</code> while
     * more complex implementations could perform type manipulation to match 
     * the column's type to the bean property type.
     * 
     * <p>
     * This implementation calls the appropriate <code>ResultSet</code> getter 
     * method for the given property type to perform the type conversion.  If 
     * the property type doesn't match one of the supported 
     * <code>ResultSet</code> types, <code>getObject</code> is called.
     * </p>
     * 
     * @param rs The <code>ResultSet</code> currently being processed.  It is
     * positioned on a valid row before being passed into this method.
     * 
     * @param index The current column index being processed.
     * 
     * @param propType The bean property type that this column needs to be
     * converted into.
     * 
     * @throws SQLException if a database access error occurs
     * 
     * @return The object from the <code>ResultSet</code> at the given column
     * index after optional type processing or <code>null</code> if the column
     * value was SQL NULL.
     */
    protected Object processColumn(ResultSet rs, int index, Class<?> propType)
        throws SQLException {
        
        if ( !propType.isPrimitive() && rs.getObject(index) == null ) {
            return null;
        }
        
        if ( !propType.isPrimitive() && rs.getObject(index) == null ) {
            return null;
        }

        if (propType.equals(String.class)) {
            return rs.getString(index);

        } else if (
            propType.equals(Integer.TYPE) || propType.equals(Integer.class)) {
            return (rs.getInt(index));

        } else if (
            propType.equals(Boolean.TYPE) || propType.equals(Boolean.class)) {
            return (rs.getBoolean(index));

        } else if (propType.equals(Long.TYPE) || propType.equals(Long.class)) {
            return (rs.getLong(index));

        } else if (
            propType.equals(Double.TYPE) || propType.equals(Double.class)) {
            return (rs.getDouble(index));

        } else if (
            propType.equals(Float.TYPE) || propType.equals(Float.class)) {
            return (rs.getFloat(index));

        } else if (
            propType.equals(Short.TYPE) || propType.equals(Short.class)) {
            return (rs.getShort(index));

        } else if (propType.equals(Byte.TYPE) || propType.equals(Byte.class)) {
            return (rs.getByte(index));

        } else if (propType.equals(Character.TYPE) || propType.equals(Character.class)) {
        	String cv = rs.getString(index);
        	if(cv != null && cv.length() > 0){
        		return cv.charAt(0);
        	}
            return '\0';

        } else if (propType.equals(Timestamp.class)) {
            return rs.getTimestamp(index);

        } else {
            return rs.getObject(index);
        }

    }

}

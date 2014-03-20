package jpacker.model;

import jpacker.annotation.Id;

public class IdModel extends ColumnModel{
	private boolean identity = true;
	
	
	public IdModel(SimpleProperty property, Id id) {
		super(property,id.name(),null,false);
		identity = id.identity();
	}

	public boolean isIdentity() {
		return identity;
	}
	
}

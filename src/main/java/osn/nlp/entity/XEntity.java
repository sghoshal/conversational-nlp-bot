package main.java.osn.nlp.entity;

public class XEntity
{
    private String entity;

    private boolean isRequired;

    private String entityId;

    private String action;

    public XEntity( String entity, boolean isRequired, String entityId )
    {
        this.entity = entity;
        this.isRequired = isRequired;
        this.entityId = entityId;
    }

    public void setEntity( String entity )
    {
        this.entity = entity;
    }

    public void setIsRequired( boolean isRequired )
    {
        this.isRequired = isRequired;
    }

    public void setEntityId( String entityId )
    {
        this.entityId = entityId;
    }

    public void setAction( String action )
    {
        this.action = action;
    }

    public String getEntity()
    {
        return entity;
    }

    public boolean isRequired()
    {
        return isRequired;
    }

    public String getEntityId()
    {
        return entityId;
    }

    public String getAction()
    {
        return action;
    }

    @Override
    public boolean equals(Object o)
    {
        if ( this == o ) {
            return true;
        }
        if ( ( o == null ) || ( getClass() != o.getClass() ) )
        {
            return false;
        }

        XEntity entity = (XEntity) o;

        return entityId.equals(entity.entityId);
    }

    @Override
    public int hashCode()
    {
        return entityId.hashCode();
    }

    @Override
    public String toString() {
        return "XEntity{" +
                "entity='" + entity + '\'' +
                ", isRequired=" + isRequired +
                ", entityId='" + entityId + '\'' +
                ", action='" + action + '\'' +
                '}';
    }
}

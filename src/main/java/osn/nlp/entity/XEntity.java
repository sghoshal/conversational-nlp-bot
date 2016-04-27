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
}

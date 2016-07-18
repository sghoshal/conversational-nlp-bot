package main.java.osn.nlp.intent;

public class XIntent
{

    private String intent;

    private String speech;

    public XIntent( String intent )
    {
        this(intent, null);
    }

    public XIntent( String intent, String speech )
    {
        this.intent = intent;
        this.speech = speech;
    }

    public String getSpeech()
    {
        return speech;
    }

    public void setSpeech(String speech)
    {
        this.speech = speech;
    }

    public String getIntent()
    {
        return intent;
    }

    public void setIntent(String intent)
    {
        this.intent = intent;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( ( o == null ) || ( getClass() != o.getClass() ) ) return false;

        XIntent xIntent = (XIntent) o;
        return intent.equals(xIntent.intent);
    }

    @Override
    public String toString()
    {
        return "XIntent{" +
                "intent='" + intent + '\'' +
                ", speech='" + speech + '\'' +
                '}';
    }

    @Override
    public int hashCode()
    {
        return intent.hashCode();
    }
}

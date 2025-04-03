package airlinetrackersystem.airlinetrackerfx;

public class AirplanePart {
    private int id;
    private String name;
    private String type;
    private int fhInspect;
    private int itInspect;

    //constructor
    public AirplanePart (int id, String name, String type, int fhInspect, int itInspect){
        this.id = id;
        this.name = name;
        this.type = type;
        this.fhInspect = fhInspect;
        this.itInspect = itInspect;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getFhInspect() {
        return fhInspect;
    }

    public int getItInspect() {
        return itInspect;
    }


}

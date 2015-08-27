import com.manydesigns.portofino.security.*
import com.manydesigns.portofino.pageactions.map.*

@RequiresPermissions(level = AccessLevel.VIEW)
class MyMap extends MapAction {

    //Automatically generated on %{new java.util.Date()} by ManyDesigns Portofino
    //Example below. Adapt it to your needs.

    @Override
    void loadObjects() {
        loadMarkers();
    }

    def loadMarkers() {

        Position markerPosition = new Position( new BigDecimal( 44.405650), new BigDecimal( 8.946256 ));
        map.addMarker( new Marker( "Genova", "ManyDesigns Office", markerPosition, "http://www.manydesigns.com"));

        markerPosition = new Position( new BigDecimal( 44.3031559), new BigDecimal(9.2097879 ));
        map.addMarker( new Marker( "Portofino", "Its purpose is to help developers create outstanding enterprise applications.", markerPosition, "http://portofino.manydesigns.com/"));
    }

}
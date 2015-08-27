

import com.manydesigns.portofino.security.*
import com.manydesigns.portofino.pageactions.gallery.*

@RequiresPermissions(level = AccessLevel.VIEW)
class MyGallery extends GalleryAction {

    //Automatically generated on %{new java.util.Date()} by ManyDesigns Portofino
    //Example below. Adapt it to your needs.

    @Override
    void loadImages() {
        gallery.addImage( new Image("Image 1","Image 1","","","/m/gallery/images/image1.png","/m/gallery/images/image1_thumb.jpg"))
        gallery.addImage( new Image("Image 2","Image 2","","","/m/gallery/images/image2.png","/m/gallery/images/image2_thumb.jpg"))
        gallery.addImage( new Image("Image 3","Image 3","","","/m/gallery/images/image3.png","/m/gallery/images/image3_thumb.jpg"))
    }

}
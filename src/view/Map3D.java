/******************************************************************************
* Title: Map3D.java
* Author: Mike Schoonover
* Date: 03/03/15
*
* Purpose:
*
* This class displays data as a 3 dimensional "topographical" map.
*
*
* Notes Regarding 3D to 2D Image Transformation
*
* World-to-Screen coordinate transformation
*
* For creating a 2-D image from a 3-D object, one of the easiest to understand,
* and most intuitive methods comes from the concept of a "pin-hole" camera.
* This method came about because it is intuitive for people to model the 3-D
* to 2-D transformation after taking a photograph because, essentially,
* that is the goal of the task, to create a realistic, photographic image
* of a 3-D object.
*
* An object in 3-D space is usually given as a set of coordinates in the
* world coordinate system, which is just a fancy name for the standard (x,y,z)
* Euclidean space. The goal is to project the 3-D object onto a viewing plane
* in such a way that the image appears realistic.
*
* The eye coordinate system was defined specifically for that purpose.
* The transformation from world coordinates into eye coordinates is a
* simple multiplication by a 3x3 matrix. When expressed in terms of
* eye coordinates, the object can be projected onto a viewing plane with a
* simple, yet elegant perspective transformation known as the magic M
* transformation. The 2-D image of the object would then be ready for
* display on screen or on paper.
*
* Basic Definitions:
*
* World Coordinate System:
* 
* The first step to modeling an object is to describe it in such a way that
* the computer can store it internally. In order to do that, an object must
* be digitized so that the computer understands the position and relative
* scale of the features of an object. This digitizing step is usually carried
* out by picking a reference point and specifying the features of an object
* relative to the reference point. The reference point is considered the
* origin, and all measurements are taken in 3-D Euclidean space
* (E3 for you math majors) in terms of X, Y, and Z components.
* This coordinate system is considered the world coordinate system.
*
* Pin-Hole Camera:
* 
* When modeling the 3-D viewing transformation using the concept of taking
* a photograph, it is useful to consider how a camera works.
* 
* First, assume that the camera is located at a single point in space.
* When taking a picture, the camera lens projects light from a certain
* "viewing volume" onto the film, forming an image. The viewing volume and
* the projection depends on the lens shape. To model the camera for the
* viewing transformation, a pyramid-shaped viewing volume is defined using
* three parameters: the near clipping plane, the far clipping plane, and
* the viewing angle. The eye is at the apex of the pyramid, and the two
* clipping planes are perpendicular to the line of sight, which runs from the
* eye through the center of the two clipping planes.
* 
* The view angle is defined as the angle formed by the opposing walls
* of the viewing volume (the cone from the eye to the target). The viewing
* volume can be illustrated with the following diagram:
* 
*      Y    X
*      |   /
*      |  /         Near Clipping Plane
*      | /          |
*      |/______     |          (   .  eye
*      |  Z         |          View Angle
*      |
*      |
*      Far clipping Plane
*
* Eye Coordinate System:
* 
* A coordinate system defined on the viewing volume. The origin is at the tip
* of the pyramid. The positive Z axis, which is the line of sight of the
* pin-hole camera, runs down the center of the pyramid. The positive Y axis
* points up looking down the Z axis. The positive X axis points to the right
* looking down the Z axis. This is a left-handed coordinate system.

* World to Eye Transformation:
* 
* Which is to take an object specified in world coordinates and transform it
* into eye coordinates. This transformation is essentially a translation and
* several rotations, and it leaves the shape and relative size of the object
* unchanged. In order to perform the transformation, the relationship between
* the eye coordinate system and the world coordinate system must be defined.
*
* This step is done as follows. First, a FROM POINT is specified in world
* coordinates. This point is where the viewer positions his eye to look
* at the object. Then an AT POINT is defined in world coordinates.
* The AT POINT along with the FROM POINT defines the line of sight.
* Then, an UP VECTOR is defined in world coordinates. The UP VECTOR determines
* the orientation of the view volume. Finally, a VIEW ANGLE is defined to
* specify the breadth of the view volume.
* 
* When these parameters have been specified, the transformation from world
* coordinates to eye coordinates is fixed. The FROM POINT is mapped to the
* origin of the eye coordinate space. The AT POINT determines the positive
* Z axis in eye coordinate space. The UP VECTOR, along with the Z axis
* determines the X axis. The Z and X axes determine the Y axis. Finally,
* the view angle determines the span of the X and Y axes in the viewing
* volume as a function of Z (distance from origin).
* 
* There are three requirements on the parameters:
* 
* The at point cannot be the same as the from point.
* The up vector cannot be parallel to the line of sight.
* The view angle must be less than 180 degrees.
*
* Eye to 2-D (magic M) Transformation:
* 
* The objects in the view volume are visible to the eye, and when a "picture"
* is taken, they are projected onto a viewing plane that is parallel to the
* clipping planes. This projection is a perspective projection, that is far
* objects appear smaller than near objects of the same size. This
* transformation is performed by setting xImg to xEye/zEye and yImg to
* yEye/zEye, where xImg and yImg are the 2-D coordinates of the image. This
* mapping projects everything in the viewing volume to a plane at zEye = 1
* where the boundary points are (1,1), (1,-1), (-1,-1), and (-1,1).
* The image on the plane can then be drawn on a screen or paper.
*
* Positioning the Final 2D Image on the Screen
* 
* After the 2D image has been created, its position on the screen can be
* adjusted for optimal viewing by adjusting xPos,yPos. This simply moves the
* final image...the view angle and zoom factor are not altered.
* 
* Open Source Policy:
*
* This source code is Public Domain and free to any interested party.  Any
* person, company, or organization may do with it as they please.
*
*/

package view;

import java.awt.*;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Map3D
//
// points[][] is used to store the input data values - each value represents
//   the height of the peak at the x,y location corresponding to the position
//   in the array
//
// s[][] is used to store the actual screen position of the data points after
//   transforming to a 3D image
//
// orthoS[][] is used to store the orthographical screen position of data point
//

class Map3D{

    //user controlled mapping parameters
    int   xPos, yPos;             // x,y position of the 2D view on the screen
    int viewAngle;                // view angle, equates to zoom in/out
                                  // (this is the angle of what is in the view)
    double xFrom, yFrom, zFrom;   // FROM POINT viewer's eyeball location
    double xAt, yAt, zAt;         // AT POINT   target's position
    double ux, uy, uz;            // UP VECTOR  view orientation
    
    int rotation;                 // degrees of rotation of the grid
    int stretchX, stretchY;       // grid spacing between points

    int xMaxPix, yMaxPix;
    
    int criticalValue;
    int warnValue;
    int normalValue;

    //grid parameters

    int dataXMax;               // the size of data grid in X direction
    int dataYMax;               // the size of data grid in Y direction

    int xMax;                   // size of the array used to store the data
    int yMax;                   //   points - it is two greater in the x and
                                //   y direction than the XMax and YMax so that
                                //   the grid will have a one element border
                                //   around the actual data points

    double xRes, yRes;              // X, Y resolution
    double zNear, zFar;             // for View volume
    double m[][] = new double[3][3];// Transformation Matrix
    double cx1, cy1, cz1;
    double cx2, cy2, cz2;
    int width, height;
    double xCenter; 
    double yCenter;

    Vertex p = new Vertex();
    Vertex orthoP = new Vertex();
    Vertex p1 = new Vertex();
    Vertex p2 = new Vertex();
    Vertex p3 = new Vertex();
    Vertex temp = new Vertex();
    
    ScreenPlane screenPlane = new ScreenPlane();
    
    //used to draw polygons
    int[] xPoints = new int[4];
    int[] yPoints = new int[4];
    Polygon quadPoly = new Polygon(xPoints, yPoints, 4); 
    
    int[] polyHeight = new int[4];

    //open file controller
    String fileName;        // input file name
    String inputFileName;   // input file name

    //Map Arrays
    int[][] points;          // input data array
    ScreenPlane[][] s;       // screen points array, corresponding to points[][]
    ScreenPlane[][] orthoS;  // screen points array, 
                             // the points value equals to zero ??? meaning?

    //map drawing controller
    boolean hiddenSurfaceViewMode;
    boolean wireFrameViewMode;
    boolean birdsEyeViewMode;
        
    static final int THRESHOLD = 0;
    static final double PI = Math.PI;
    static final double ROUND = 0.5;

    //2,4,6,8,10,12 bigger number lower resolution    
    static final double RESOLUTION = 5.0; 
        
//-----------------------------------------------------------------------------
// Map3D::Map3D (constructor)
//
// 3D graphical parameters are initialized in the constructor. The change of
// some of these parameters may be cause unexpected results.
//
// Note: While the size of the data array is established by _DataXMax and
//       _DataYMax, the arrays are not created until CreateArrays or one of
//      the data from disk loading functions are called.  If data is to be
//      added to the arrays without loading from disk, CreateArrays must first
//      be explicitly called after construction.
//
// pDataXMax, pDataYMax specify the number of data points for the grid. This
// is not the number of pixels.
//

public Map3D(int pChartGroupNum, int pChartNum, int pGraphNum,
            int pWidth, int pHeight, int pDataXMax, int pDataYMax)
{

    dataXMax = pDataXMax; dataYMax = pDataYMax;
    xMax = dataXMax + 2; yMax = dataYMax + 2;

    setCanvasSize(pWidth, pHeight);
        
    points = new int[xMax][yMax];
        
    s = new ScreenPlane[xMax][yMax];
    orthoS = new ScreenPlane[xMax][yMax];    
    
    for(int i=0; i<s.length; i++){
        for(int j=0; j<s[i].length; j++){
            s[i][j] = new ScreenPlane();
            orthoS[i][j] = new ScreenPlane();
        }
    }

    viewAngle = 0;              // amount of target viewed
    rotation = 0;               // rotation of target in world space
    xPos = 0; yPos = 0;         // position of 2D image on screen

    xRes = 10; yRes = 5;        // resolution of X, Y
    ux = 0.0; uy = 0.0; uz = 1; // up points, x, y, z
    zNear = -10; zFar = 100;    // To control view volumn
    cx1 = 0.0; cy1 = 0.0; cz1 = 0.1;
    cx2 = 0.0; cy2 = 0.0; cz2 = 0.0;
    
    stretchX = 1; stretchY = 1; // grid spacing
    
    // initialize the magic transformation matrix m[][], which is used to
    // transform world coordinate 3d to 2d screen coordinate
    for (int i=0; i<3; i++){
        for (int j=0; j<3; j++){ m[i][j] = 0.0; }
    }

    // default is Hidden Surface Removal
    hiddenSurfaceViewMode = true;
    wireFrameViewMode = false;
    birdsEyeViewMode = false;

    //default input file name
    inputFileName = "MapInputFile.map";
        
}//end of Map3D::Map3D (constructor)
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3D::init
//
// Initializes the object.  Must be called immediately after instantiation.
//

public void init()
{

    
}// end of Map3D::init
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3D::setCanvasSize
//
// Sets the size of the canvas upon which the map is to be drawn. May be
// called at any time after creation to adjust the size.
//
// If either pWidth or pHeight is Integer.MAX_VALUE, that value will be
// ignored and not modified.
//

final void setCanvasSize(int pWidth, int pHeight)
{
    
    if (pWidth != Integer.MAX_VALUE){
        xCenter = pWidth / 2; xMaxPix = pWidth - 1;
    }
        
    if (pHeight != Integer.MAX_VALUE){    
        yCenter = pHeight / 2; yMaxPix = pHeight - 1;
    }
    
}// end of Map3D::setCanvasSize
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3D::addRow
//
// Adds a new row of data to the 3D grid.
//

public void addRow(int[] pDataSet)
{

    
}// end of Map3D::addRow
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Map3D::resetAll
//
// Resets all values and child values to default.
//

public void resetAll()
{
    
    
}// end of Map3D::resetAll
//-----------------------------------------------------------------------------

//---------------------------------------------------------------------------
// Map3D::createArrays
//
// This function allocates memory for the arrays used for mapping.
//
// points[][] is used to store the input data values - each value represents
//   the height of the peak at the x,y location corresponding to the position
//   in the array
//
// s[][] is used to store the actual screen position of the data points after
//   transforming to a 3D image
//
// orthoS[][] is used to store the orthographical screen position of data point
//

public void createArrays()
{

    points = new int[xMax][yMax];
        
    s = new ScreenPlane[xMax][yMax];
    orthoS = new ScreenPlane[xMax][yMax];    

    for(int i=0; i<s.length; i++){
        for(int j=0; j<s[i].length; j++){
            s[i][j] = new ScreenPlane();
            orthoS[i][j] = new ScreenPlane();            
        }
    }
    
}// end of Map3D::createArrays
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// Map3D::fillInputArray
//
// This function is used to set input points array values to pValue.
//

public void fillInputArray(int pValue)
{

    for ( int i = 0; i < xMax; i++){
        for ( int j = 0; j < yMax; j++){ points[i][j] = pValue; }
    }

}//end of Map3D::fillInputArray
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::setDataRow
//
// This function is used to set a single row of data in the points array.
// _DataRow should point to an array having the same number of elements as is
// present in one row of the points array.
//
// Since the data is stored in an array that is actually two elements larger
// in both the x and y directions, the XPos and YPos are offset by one before
// storing. The extra elements are used to form a border around the map which
// is always at level 0.
//

public void setDataRow(int _XPos, int[] _DataRow)
{

    assert(_XPos < dataXMax);

    System.arraycopy(_DataRow, 0, points[_XPos + 1], 1, dataYMax);

}// end of Map3D::setDataRow
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// Map3D::setDataPoint
//
// Sets a single data point in the data array at position _XPos, _YPos.
//
// Since the data is stored in an array that is actually two elements larger
// in both the x and y directions, the XPos and YPos are offset by one before
// storing. The extra elements are used to form a border around the map which
// is always at level 0.
//

public void setDataPoint(int _XPos, int _YPos, int _Value)
{

    assert(_XPos < dataXMax && _YPos < dataYMax);

    points[_XPos + 1][_YPos + 1] = _Value;

}//end of Map3D::setDataPoint
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// Map3D::worldToScreen
//
// This function is used to set the value of world coordinate for the map,
// and transform the world coordinate to the screen coordinate.
//

//debug mks -- function uses class Degree rather than _Degree -- should use _Degree

private void worldToScreen(int _Degree, int _StretchX, int _StretchY)
{

    double co, si, tmp1, tmp2;

    // int maxZ; //Z-axis length

    co = Math.cos( PI * (double)rotation / 180.0 );
    si = Math.sin( PI * (double)rotation / 180.0 );

    for ( int i = 0; i < xMax; i++){
        
        for ( int j = 0; j < yMax; j++){

            // set value (x,y,z) of every point in the world coordinate
            p.x = (i - xMax / 2.0) * _StretchX;
            p.y = (j - yMax / 2.0) * _StretchY;
            p.z = points[i][j];

            // set value of orthograghic point of p
            orthoP.x = (i - xMax / 2.0) * _StretchX;
            orthoP.y = (j - yMax / 2.0) * _StretchY;
            orthoP.z = 0;

            // Rotation Z-axis
            tmp1 = p.x * co - p.y * si;
            tmp2 = p.x * si + p.y * co;
            p.x=tmp1;
            p.y=tmp2;

            // Rotation Z-axis
            tmp1 = orthoP.x * co - orthoP.y * si;
            tmp2 = orthoP.x * si + orthoP.y * co;
            orthoP.x=tmp1;
            orthoP.y=tmp2;

            // get value of screen plane points
            vTrans3Dto2D(s[i][j], p);
            vTrans3Dto2D(orthoS[i][j], orthoP);
            }
        }

}// end of TopographicalMapper::WorldToScreen
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
//Map3D::vTrans3Dto2D
//
// This function is used to transform the 3D world coordinate to the 2D screen
// coordinate using the magic transformation matrix m[][].
//
// The input point is in pSP and the transformed output will be placed in
// pPlane.
//

private void vTrans3Dto2D(ScreenPlane pPlane, Vertex pSP)
{

    double vx, vy, vz;
    double ex, ey, ez;

    // Transform each vertex to eye coord.

    vx = pSP.x - xFrom; vy = pSP.y - yFrom; vz = pSP.z - zFrom;
    
    ex = vx * m [0][0] + vy * m [0][1] + vz * m [0][2];
    ey = vx * m [1][0] + vy * m [1][1] + vz * m [1][2];

    //    ez = vx * m [2][0] + vy * m [2][1] + vz * m [2][2];
    // Orthographic view volume. In Ortho, ez is a constant    --fqz
    ez = RESOLUTION;

    //  Translate to screen coordinates.
    pPlane.x = (int)((cx1 * ex / ez + cx2 + ROUND) + xCenter + xPos);
    pPlane.y = (int)(yRes-(cy1 * ey / ez + cy2 + ROUND)+ yCenter + yPos);
    ez = cz1 - cz2 / ez;

    // Count Z buffer
    //    if (ez < 0)        ez = 0;
    //    if (ez > 65535.0)  ez = 65535;

    pPlane.zDepth = (int)ez;

}// end of Map3D::vTrans3Dto2D
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// Map3D::calculate
//
// This function is used to calculate the value of magic matrix m[3][3],
// which is related to From Point (fx, fy, fz) and At Point (ax, ay, az).
//

private void calculate()
{

    double    norm;

    //protect map data
    if(zAt > 25) zAt = 25; if(viewAngle < -5) viewAngle = -5;

    // World-to-Eye transformation
    // Get transformation Matrix
    // Compute Z axis
    p1.x = xAt - xFrom;
    p1.y = yAt - yFrom;
    p1.z = zAt - zFrom;
    norm = Math.sqrt (p1.x * p1.x + p1.y * p1.y + p1.z * p1.z);

    if (norm!=0) {
        m [2][0] = p1.x / norm;
        m [2][1] = p1.y / norm;
        m [2][2] = p1.z / norm;
    }
    else{
       // wip mks -- replace this with Java or remove it?
       // MessageDlg("Calc Znorm=0",mtInformation, TMsgDlgButtons()<<mbOK,0);
    }
        
    //compute X axis
    temp.x = ux;
    temp.y = uy;
    temp.z = uz;
    vCross (p1, temp, p2);

    norm = Math.sqrt (p2.x * p2.x + p2.y * p2.y + p2.z * p2.z);
    if (norm!=0){
        m [0][0] = p2.x / norm;
        m [0][1] = p2.y / norm;
        m [0][2] = p2.z / norm;
        }
    else{
       // wip mks -- replace this with Java or remove it?        
       //MessageDlg("Calc Xnorm = 0", mtInformation, TMsgDlgButtons()<<mbOK, 0);
    }
        
    //compute Y axis //debug mks -- move vCross and "norm =" before if statement?
    if(norm!=0){
        vCross (p2, p1, p3);
        norm = Math.sqrt (p3.x * p3.x + p3.y * p3.y + p3.z * p3.z);
        m [1][0] = p3.x / norm;
        m [1][1] = p3.y / norm;
        m [1][2] = p3.z / norm;
        }
    else{
       // wip mks -- replace this with Java or remove it?        
       //MessageDlg("Calc Ynorm = 0", mtInformation, TMsgDlgButtons()<<mbOK, 0);
    }
        
    // Magic M transformation
    norm = Math.tan ((viewAngle) * PI / 180);
    cx1 = xRes /  norm;
    cx2 = xRes / 2.0;
    cy1 = yRes / norm;
    cy2 = yRes / 2.0;
    cz1 = Integer.MAX_VALUE * zFar / (zFar - zNear);
    cz2 = cz1 * zNear;

}//end of Map3D::calculate
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// Map3D::vCross
//

private void vCross(Vertex p1, Vertex p2, Vertex p)
{
    p.x = p1.y * p2.z - p2.y * p1.z;
    p.y = p2.x * p1.z - p1.x * p2.z;
    p.z = p1.x * p2.y - p2.x * p1.y;

}// end of Map3D::vCross
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// Map3D::Paint
//
// This function is used to receive parameters sent by the caller function,
// and call all functions to paint a map on the image, then transfer this image
// to the paintbox.
//
// To avoid the flicker problem, we first plot the polygons and lines on to a
// image, which is invisible to viewer, then transfer the image to a bitmap,
// finally, draw this bitmap on the paintbox, which is visible to the viewer.
//
// The background is not cleared...Java seems to be doing that when
// repainting the container panel. May need to add a clearing function for other
// programming environments.
//
// Note by MKS: as converted to Java, this method now paints directly to 
// the screen without buffering to prevent flicker. The buffering can be
// implemented later if necessary.
//

public void paint(Graphics2D pG2, 
            Map3DViewParameters pViewParams,
            int pStretchX, int pStretchY,
            boolean pHiddenSurfaceViewMode, boolean pWireFrameViewMode,
            boolean pBirdsEyeViewMode, int pCriticalValue,
            int pWarnValue, int pNormalValue)
{

//if data arrays have not yet been created, do nothing
if (points == null) return;

//store view parameters

xFrom = pViewParams.xFrom; yFrom = pViewParams.yFrom; zFrom = pViewParams.zFrom;
xAt = pViewParams.xAt; yAt = pViewParams.yAt; zAt = pViewParams.zAt;
xPos = pViewParams.xPos; yPos = pViewParams.yPos;
rotation = pViewParams.rotation;
viewAngle = pViewParams.viewAngle;
stretchX = pStretchX; stretchY = pStretchY;

criticalValue = pCriticalValue;
warnValue = pWarnValue;
normalValue = pNormalValue;

hiddenSurfaceViewMode = pHiddenSurfaceViewMode; //show hidden line view if true
wireFrameViewMode = pWireFrameViewMode; //show wire frame view if true
birdsEyeViewMode = pBirdsEyeViewMode; //show bird's eye view if true

// calculate the mapping parameters and the magic matrix
calculate();

// 3D-2D coordinate transformation
worldToScreen(rotation, stretchX, stretchY);

pG2.setColor(Color.BLACK);

//create the image on a hidden canvas (to avoid flicker)
if(hiddenSurfaceViewMode) hiddenSurfaceDraw(pG2);
if(wireFrameViewMode) drawWireFrame(pG2);
if(birdsEyeViewMode) birdsEyeView(pG2);

}//end of Map3D::paint
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// Map3D::birdsEyeView
//
// This function is used to show a bird's eye view of the input data.
// In other words, the top view of the orthograhic projection.
//
// debug mks -- not tested yet in Java...why is 850 hardcoded? needs to be
// related to canvas size?
//

private void birdsEyeView(Graphics2D pG2)
{

    for ( int i = 0; i < xMax; i++){

        pG2.setColor(Color.BLUE);

        pG2.drawLine(xMaxPix-i*10, 0, xMaxPix-i*10, 0);

        for ( int j = 0; j < yMax; j++){
            
            if (points[i][j] > THRESHOLD){
                pG2.setColor(Color.RED);
                pG2.drawLine(xMaxPix-i*10, j*10,xMaxPix-i*10, j*10);
            }
        }
    }

} // end of Map3D::birdsEyeView
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// map3D::drawWireFrame
//
// This function is used to draw the wireframe map of the input data.
//

private void drawWireFrame(Graphics2D pG2)
{

    //draw data points which are connected with lines

    for ( int i = 0; i < xMax - 1 ; i++){
        for ( int j = 0; j < yMax - 1 ; j++){
            
            pG2.drawLine(s[i][j].x, s[i][j].y, s[i+1][j].x, s[i+1][j].y);
            
            pG2.drawLine(s[i][j].x, s[i][j].y, s[i][j+1].x, s[i][j+1].y);
        }
    }

    // draw the left border
    
    for ( int i = 0; i < xMax - 1 ; i++) {
        pG2.drawLine(s[i][yMax -1].x, s[i][yMax -1].y,
                                    s[i+1][yMax -1].x, s[i+1][yMax -1].y);
    }
    
}//end of map3D::drawWireFrame
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// Map3D::hiddenSurfaceDraw
//
// This function is used to draw data points on the image with hidden surface
// removal.
//
// Polygonal representation of three-dimensional objects is the classic
// representational form in three-dimensional graphics. An object is represented
// by a mesh of polygonal facets. In the general cases an object possesses
// curved surfaces and the facets could be represented by several polygons. In
// our problem, we generate the map using the polygon modelling method with
// the mathematical description.
//
// From the wire frame map, we generalize that the map could be decomposed into
// many triangles and quadrilaterals. With BCB graphical functions, such as,
// Canvas->Polygon(), we could plot the polygons easily. Anothor important
// thing to be considered is the hidden surface removal. Basically, if we
// draw an object, like the pyramid, we need draw the back facet first, then
// draw the front facet, so the viewer can only see the front facet. This
// order will never change. But in practice, the order, which one is front
// facet and which one is back facet, will be changed according to the change
// of coordinate. So when we change the rotation degree, the coordinate will
// change, and the order of drawing facets must change to suit the hidden
// surface removal requirement. In this program, we change the order of drawing
// facets every 45 degree. In addition, in every 45 degree mapping operation,
// we must decide to draw row(X) first or column(Y) first. It is determined by
// the visual effect. In general, we draw the pyramids (triangles) first, then
// quadrilaterals.
//

private void hiddenSurfaceDraw(Graphics2D pG2)
{

    // draw the grid without peaks - flat plane

    for ( int i = 0; i < xMax - 1; i++)
        for ( int j = 0; j < yMax - 1; j++){
            pG2.drawLine(orthoS[i][j].x, orthoS[i][j].y,
                                orthoS[i+1][j].x, orthoS[i+1][j].y); //plot row
            pG2.drawLine(orthoS[i][j].x, orthoS[i][j].y,
                             orthoS[i][j+1].x, orthoS[i][j+1].y); //plot column
            }

    if(pG2 == null) { return; } //debug mks -- remove this
    
    // draw the closing edges of the grid

    //along the Y axis
    for (int i = 0; i < xMax - 1; i++){
        pG2.drawLine(s[i][yMax - 1].x, s[i][yMax - 1].y,
                                    s[i + 1][yMax - 1].x, s[i + 1][yMax - 1].y);
        }

    //along the X axis
    for (int i = 0; i < yMax - 1; i++){
        pG2.drawLine(s[xMax - 1][i].x, s[xMax - 1][i].y,
                                    s[xMax - 1][i + 1].x, s[xMax - 1][i + 1].y);
        }

    // Uncomment the next two lines to display a vertical line at the 0,0 point
    // of the 3D image grid - this is useful when debugging.
    // image->Canvas->MoveTo(s[0][0].x, s[0][0].y);
    // image->Canvas->LineTo(s[0][0].x, s[0][0].y - 150);


    //draw the polygons to create the 3D image
    //the non data zero point values along the edges of the grid are also
    //processed because they are part of the polygons connected with the
    //edgemost data points

    //drawing always starts at the furthest corner from the viewer and commences
    //forward towards the viewer so that the front faces cover the back faces -
    //the starting point on the grid and direction of drawing depend on the
    //current viewing angle so that drawing always starts at the furthest corner

    int XStart, XStop, XDirection, YStart, YStop, YDirection;
    int XPolyDirection, YPolyDirection;

    if ((rotation >= 0 && rotation < 45) || (rotation >= 315 && rotation < 360)){
        XStart = 1; XStop = xMax; XDirection = 1; XPolyDirection = -1;
        YStart = 1; YStop = yMax; YDirection = 1; YPolyDirection = -1;
        }
    else
    if (rotation >= 45 && rotation < 135){
        XStart = 1; XStop = xMax; XDirection = 1; XPolyDirection = -1;
        YStart = yMax - 2; YStop = -1; YDirection = -1; YPolyDirection = 1;
        }
    else
    if (rotation >= 135 && rotation < 225){
        XStart = xMax - 2; XStop = -1; XDirection = -1; XPolyDirection = 1;
        YStart = yMax - 2; YStop = -1; YDirection = -1; YPolyDirection = 1;
        }
    else
    if (rotation >= 225 && rotation < 315){
        XStart = xMax - 2; XStop = -1; XDirection = -1; XPolyDirection = 1;
        YStart = 1; YStop = yMax; YDirection = 1; YPolyDirection = -1;
        }
    else
        return;

    //draw all the polygons to create the 3D image
    drawPolygons(pG2, XStart, XStop, XDirection, XPolyDirection,
                             YStart, YStop, YDirection, YPolyDirection, false);

}// end of Map3D.hiddenSurfaceDraw
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// Map3D::drawPolygons
//
// Draws the polygons to create the 3D image.  The polygons are drawn on the
// TCanvas object pointed by _Canvas.
//
// XStart/XStop/XDirection specifies the X index of the data points at which to
// start and stop drawing and the direction of iteration for that index.
// XPolyDirection specifies the direction from the base point of the other
// points to use in forming a quadrilateral relative to the X index.
//
// YStart/YStop/YDirection specifies the Y index of the data points at which to
// start and stop drawing and the direction of iteration for that index.
// YPolyDirection specifies the direction from the base point of the other
// points to use in forming a quadrilateral relative to the Y index.
//
// To ensure that polygon faces at the front of the view hide those on the back
// side, parameters should specify that drawing starts from the opposite corner
// of the data grid with respect to the position of the viewer.
//
// XPolyDirection and YPolyDirection should specify a direction towards the
// opposite corner.  The first point of each polygon will thus be closest to
// the viewer and the remaining points will be towards the opposite corner
// of the grid.
//
// The parameters must be selected for the current rotation angle of the view.
// See the HiddenSurfaceDraw function for examples of parameters for the
// different ranges of angular rotation.
//
// This function is stand-alone so that it may be called to draw a single row
// of data points.  This is useful for high speed data tracking operations
// because it requires less time to draw the grid row by row as new data is
// added rather than redrawing the entire image each time.
//
// If _ClearAreaAboveGrid is true, the area above the grid will be cleared
// to erase any left over graphics from a previous rendering.  This is valid
// only for specific conditions and viewing angles.  It is useful when the
// function is being called by function QuickDrawSingleRow during real time
// data display.  Because this mode does not render and clear the entire screen
// but instead draws one row at a time, old graphics are not cleared above the
// grid plane.
//
// Example 1: erasing option is valid when viewing the map at a rotational
//          angle of 135 degrees and data progression is from left to right.
//

private void drawPolygons(
                Graphics2D pG2,
                int _XStart, int _XStop, int _XDirection, int _XPolyDirection,
                int _YStart, int _YStop, int _YDirection, int _YPolyDirection,
                boolean _ClearAreaAboveGrid)

{

    int j;

    //polygon outlines are black
    pG2.setColor(Color.BLACK);

    for(int i = _XStart; i != _XStop; i += _XDirection){

        //clear area above the grid plane from the edge to the top of the canvas
        //this erases the "sky" - see function header for more info
        //this is done by redrawing the polygons with background and foreground
        //colors set to the background color of the graphics object
        
        if(_ClearAreaAboveGrid){

            j = _YStart;

            quadPoly.xpoints[0] = s[i][j + _YPolyDirection].x;
            quadPoly.ypoints[0] = s[i][j + _YPolyDirection].y;

            quadPoly.xpoints[1] = s[i + _XPolyDirection][j + _YPolyDirection].x;
            quadPoly.ypoints[1] = s[i + _XPolyDirection][j + _YPolyDirection].y;
            
            quadPoly.xpoints[2] = s[i + _XPolyDirection][j + _YPolyDirection].x;
            quadPoly.ypoints[2] = 0;
            
            quadPoly.xpoints[3] = s[i][j + _YPolyDirection].x;
            quadPoly.ypoints[3] = 0;

            quadPoly.invalidate(); //force use of new data
            
            //erase to background color
            pG2.setColor(pG2.getBackground());
            
            //draw the quadrilateral on the hidden image canvas
            pG2.draw(quadPoly);

            //polygon outlines are black - return pen color to black
            pG2.setColor(Color.BLACK); //debug mks -- actually need to set color used to paint sides
                                       //C++ painted outlines with pen and panels with brush
                                       //Java paints both with current color but uses two different commands

        }


        for (j = _YStart; j != _YStop; j += _YDirection){

            //assign the points around the current point to a quadrilateral
            //(the other three points are toward the far sides of the grid
            // from the viewer)
            
            quadPoly.xpoints[0] = s[i][j].x;
            quadPoly.ypoints[0] = s[i][j].y;
            
            quadPoly.xpoints[1] = s[i + _XPolyDirection][j].x;
            quadPoly.ypoints[1] = s[i + _XPolyDirection][j].y;
            
            quadPoly.xpoints[2] = s[i + _XPolyDirection][j + _YPolyDirection].x;
            quadPoly.ypoints[2] = s[i + _XPolyDirection][j + _YPolyDirection].y;
                                    
            quadPoly.xpoints[3] = s[i][j + _YPolyDirection].x;
            quadPoly.ypoints[3] = s[i][j + _YPolyDirection].y;

            quadPoly.invalidate(); //force use of new data
            
            //get the height for each point - these are the values from the
            //original data input array - the viewing array x,y points are
            //distorted to show the heights as a 3D image on a 2D screen and
            //thus do not make sense for this purpose
            
            polyHeight[0] = points[i][j];
            polyHeight[1] = points[i + _XPolyDirection][j];
            polyHeight[2] = points[i + _XPolyDirection][j + _YPolyDirection];
            polyHeight[3] = points[i][j + _YPolyDirection];
            
            //assign a color to the quadrilateral based on the height of its
            //highest point
            pG2.setColor(assignColor(pG2, polyHeight));
            //draw the quadrilateral on the hidden image canvas
            pG2.fill(quadPoly);
            
            //outline the polygon in black to give it definition
            pG2.setColor(Color.BLACK);
            pG2.draw(quadPoly);
            

        }

    }

}// end of Map3D:DrawPolygons
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// Map3D::assignColor
//
// Determines the color for a polygon based on the height of its highest
// corner point.  The heights of the four corners are passed via the array
// pHeight.
//

private Color assignColor(Graphics2D pG2, int[] pHeight)
{

    if (pHeight[0] >= criticalValue || pHeight[1] >= criticalValue
       || pHeight[2] >= criticalValue || pHeight[3] >= criticalValue)
        return(Color.RED);
    else
    if (pHeight[0] >= warnValue || pHeight[1] >= warnValue
       || pHeight[2] >= warnValue || pHeight[3] >= warnValue)
        return(Color.GREEN);
    else
    if (pHeight[0] > normalValue || pHeight[1] > normalValue
       || pHeight[2] > normalValue || pHeight[3] > normalValue)
        return(Color.BLUE);
    else
    if (pHeight[0] > 0 || pHeight[1] > 0 || pHeight[2] > 0 || pHeight[3] > 0)
        return(Color.LIGHT_GRAY);
    else
        return(pG2.getBackground());

}//end of Map3D.assignColor
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// Map3D::QuickDrawSingleRow
//
// Draws a single row of polygons for the points in the data grid row specified
// by _DataY.  The current values for viewing angle and other parameters are
// used, so the map should already have been drawn once using paint method to
// set these parameters as desired.
//
// The polygons are drawn directly on the visible canvas to improve speed. The
// parameters _XStart, _XStop, _XDirection, _XPolyDirection, _YStart, _YStop,
//  _YDirection, _YPolyDirection are described in the DrawPolygons function
//  header.  These parameters are dependent on the current viewing angle.
//
// This function is most useful for real time graphing when new data points are
// added to the grid as they are obtained.  Redrawing the entire map is time
// consuming while drawing a single row is more efficient.
//

void quickDrawSingleRow(Graphics2D pG2,
                int _XStart, int _XStop, int _XDirection, int _XPolyDirection,
                int _YStart, int _YStop, int _YDirection, int _YPolyDirection,
                boolean _ClearAreaAboveGrid)
{


    //perform 3D-2D coordinate transformation so any new data points are
    //processed using the previously set viewing parameters

    worldToScreen(rotation, stretchX, stretchY);

    //draw all the polygons to create the 3D image - the polygons are drawn
    //directly onto the visible canvas so it is not necessary to transfer the
    //hidden image

    drawPolygons(pG2, _XStart, _XStop, _XDirection, _XPolyDirection,
           _YStart, _YStop, _YDirection, _YPolyDirection, _ClearAreaAboveGrid);

}//end of Map3D::quickDrawSingleRow
//---------------------------------------------------------------------------

}//end of class Map3D
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class ScreenPlane
//

class ScreenPlane{

    int x;
    int y;
    int zDepth;
    
}//end of class ScreenPlane
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class Vertex
//

class Vertex{

    double x;
    double y;
    double z;
    
}//end of class Vertex
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

//---------------------------------------------------------------------------

/*


#include <vcl.h>
#include <exception>
#include <iostream>
#include <stdarg.h>
#include <jpeg.hpp>
#include <fstream.h>
#include <assert.h>

#pragma hdrstop
#pragma package(smart_init)

#include "TopographicalMapper.h"

const int    THRESHOLD = 0;
const double PI = 3.1415926;
const double ROUND = 0.5;
const double CENTRE_X = 350.0; // centre of the screen = half of the screen's X, Y
const double CENTRE_Y = 200.0;
const double RESOLUTION = 5.0; //2, 4,6, 8, 10 ,12 bigger number lower resolution

//---------------------------------------------------------------------------
// TopographicalMapper::TopographicalMapper (constructor)
//
// 3D graphical parameters are initialized in the constructor. The change of
// some of these parameters may be cause unexpected results.
//
// Note: While the size of the data array is established by _DataXMax and
//       _DataYMax, the arrays are not created until CreateArrays or one of
//      the data from disk loading functions are called.  If data is to be
//      added to the arrays without loading from disk, CreateArrays must first
//      be explicitly called after construction.
//

__fastcall TopographicalMapper::TopographicalMapper(
                    int _DataXMax,  // size of data grid along x axis
                    int _DataYMax,      // size of data grid along y axis
                    TPaintBox* _paintbox    //paintbox pointer
                    ):

                    DataXMax(_DataXMax), DataYMax(_DataYMax),
                    XMax(_DataXMax + 2), YMax(_DataYMax + 2),
                    points(NULL), s(NULL), orthoS(NULL),
                    paintbox(_paintbox),

                    //following parameter can't be changed
                    Xres(10), Yres(5),      // resolution of X, Y
                    ux(0.0),uy(0.0),uz(1),  // up points, x, y, z
                    zNear(-10), zFar(100),  // To control view volumn
                    angle(6),               // zoom in/out
                    cx1(0.0), cy1(0.0), cz1(0.1),
                    cx2(0.0), cy2(0.0), cz2(0.0),
                    fx(15),fy(9),fz(26),    // from points, x, y, z
                    ax(11), ay(5), az(20),  // at points, x,y, z
                    dx(0),dy(0),            // change with the central position
                    dAngle(0),              // zoom in/out
                    Degree(0),              // rotation angle
                    StretchX(1),StretchY(1) // grid spacing
{

//Label1 is used to show the value of peak within the mouse move event
Label1 = new TLabel(paintbox);

image = new TImage(paintbox);
image->Visible = true;
image->Width  = paintbox->Width;
image->Height = paintbox->Height;

// initialize the magic transformation matrix m[][], which is used to
// transform world coordinate 3d to 2d screen coordinate
for ( int i= 0; i < 3; i++)
    for ( int j = 0; j< 3; j++) m[i][j] = 0.0;

//Left and right mouse button to control the image rotation and movement.
mouseDown=false;
rmouseDown=false;

// Within the zone, the mouse can control the image movement and rotation.
zone.Left= paintbox->Left;
zone.Top= paintbox->Top;
zone.Right= paintbox->Left+paintbox->Width;
zone.Bottom= paintbox->Top+paintbox->Height;

// default is Hidden Surface Removal
HiddenSurfaceViewMode = true;
WireFrameViewMode = false;
BirdsEyeViewMode = false;

criticalValue=100;
warnValue=65;
normalValue=25;

//default input file name
InputFileName = "MapInputFile.map";

//create TBitmap to transfer the image drawn on the hidden bitmap to the screen
TransferBitmap = new Graphics::TBitmap;
TransferBitmap->Width  = paintbox->Width;
TransferBitmap->Height = paintbox->Height;

}//end of TopographicalMapper::TopographicalMapper (constructor)
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::~TopographicalMapper (destructor)
//

__fastcall TopographicalMapper::~TopographicalMapper(void)
{

delete(TransferBitmap);

DeleteArrays();

}//end of TopographicalMapper::~TopographicalMapper (destructor)
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::DeleteArrays
//
// Deletes all dynamically allocated arrays.
//

void __fastcall TopographicalMapper::DeleteArrays(void)
{

if (!points) return;

delete[] points;        points = NULL;
delete[] s;             s = NULL;
delete[] orthoS;        orthoS = NULL;

}//end of TopographicalMapper::DeleteArrays
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::OpenFile
//
// Allows user to select a data input file and loads map data form that file.
// The file should have ".map" extension. For details of the file format see
// the comments of LoadData() function.
//
// The default filename is set to the selected filename.
//

void __fastcall TopographicalMapper::OpenFile(void)
{

TOpenDialog *OpenDialog1 = new TOpenDialog(paintbox);

// only open ".map" or "MAP"
OpenDialog1->DefaultExt = ".map";
OpenDialog1->Filter = "map (*.map)|*.MAP";
OpenDialog1->Options << ofOverwritePrompt << ofFileMustExist << ofHideReadOnly;

//get filename
if (OpenDialog1->Execute()) InputFileName = OpenDialog1->FileName;

//open file and load data
LoadData();

}// end of TopographicalMapper::OpenFile
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::OpenFile
//
// Sets the default map data input filename to _Filename and loads map data
// from that file.
//

void __fastcall TopographicalMapper::OpenFile(AnsiString _Filename)
{

//set the default filename
InputFileName = _Filename;

//open file and load data
LoadData();

}//end of TopographicalMapper::OpenFile
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::LoadData
//
// Loads data from the file having filename stored in InputFileName.
// The first line should contain two numbers - the first is the maximum
// number of columns of data (this is the X axis of the map), the second is
// the number of rows of data (this is the Y axis of the map).
//
// Example format:
//
//          22 10                   (X Max, Y Max)
//          0  0  0  0  1  1 ...    (data line, values separated by blanks)
//          0  1  10 1  0  0 ...
//          (total 10 rows and 22 columns)
//
// Since the data is stored in an array that is actually two elements larger
// in both the x and y directions, the XPos and YPos are offset by one before
// storing into the array.  Thus the user sees the data as starting at position
// 0 but it is actually stored starting at position 1.
//


void __fastcall TopographicalMapper::LoadData(void)
{

//open input file
ifstream input;
input.open(InputFileName.c_str());

if (!input){
    MessageDlg("Could not open Data Input File",
    mtWarning, TMsgDlgButtons()<<mbOK, 0);
    return;
    }

//get the size of the entity array.
input >> DataXMax >> DataYMax;

//the array used to hold the data points is larger than the maximum number of
//data points so there will be a border of points with zero value around the
//outside of the data points - this allows the outside points to be drawn
//properly as they will rise above the rim of the grid
//
//usually, the user need not be concerned with the actual size of the array -
//all data insertion and retrieval should be done via functions which will
//translate the input positions to the proper offset in the array - input data
//will appear to start at index 0 and stop at XMax - 1/YMax - 1 even though it
//is actually stored in the array from 1 to XMax - 2/YMax - 2

XMax = DataXMax + 2; YMax = DataYMax + 2;

//creating arrays according the size of x,y
CreateArrays();

// initialize arrays with zero
ZeroGridArray();

//load data into the data array, shifting it by 1 to avoid the border of non
//data zero values in the points array

for ( int y = 0; y < DataYMax; y++)
    for ( int x = 0; x < DataXMax; x++){
        if (!input) break;
        input >> points[x + 1][y + 1];
        }

//close input file
input.close();

}//end of TopographicalMapper::LoadData()
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::CreateArrays
//
// This function allocates memory for the arrays used for mapping.  If the
// arrays have already been created, they are destroyed and recreated so the
// array sizes will match the current settings.
//
// points[][] is used to store the input data values - each value represents
//   the height of the peak at the x,y location corresponding to the position
//   in the array
//
// s[][] is used to store the actual screen position of the data points after
//   transforming to a 3D image
//
// orthoS[][] is used to store the orthographical screen position of data point
//

void __fastcall TopographicalMapper::CreateArrays(void)
{

// first, free memory if arrays previously allocated
DeleteArrays();

// make array to hold input data points
try {
    points = new int*[XMax];
    for (int i = 0; i < XMax; i++) points[i] = new int[YMax];
    }
catch (std::bad_alloc){
    //wip - need to clean up any array that has been partially created
    points = NULL;
    MessageDlg("Could not allocate array.",
    mtWarning, TMsgDlgButtons() << mbOK, 0);
    exit(-1);
    }

//create an array for screen coordinates of 3D image points and an array for
//holding the orthogonal position of each point

try {
    s = new ScreenPlane*[XMax];
    orthoS=new ScreenPlane*[XMax];
    for ( int i = 0; i < XMax; i++) s[i] = new ScreenPlane[YMax];
    for ( int i = 0; i < XMax; i++) orthoS[i]= new ScreenPlane[YMax];
    }
catch (std::bad_alloc){
    MessageDlg("Could not allocate array.",
    mtWarning, TMsgDlgButtons() << mbOK, 0);
    exit(-1);
    }

}// end of TopographicalMapper::CreateArrays
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::ZeroGridArray
//
// This function is used to set points[][]'s value to zero.
//

void __fastcall TopographicalMapper::ZeroGridArray(void)
{

for ( int i = 0; i < XMax; i++)
    for ( int j = 0; j < YMax; j++) points[i][j] = 0;

}//end of TopographicalMapper::ZeroGridArray
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::LoadDataRow
//
// This function is used to load a single row of data into the points array.
// _DataRow should point to an array having the same number of elements as is
// present in one row of the points array.
//
// Since the data is stored in an array that is actually two elements larger
// in both the x and y directions, the XPos and YPos are offset by one before
// storing into the array.  Thus the user sees the data as starting at position
// 0 but it is actually stored starting at position 1.
//

void __fastcall TopographicalMapper::LoadDataRow(int _XPos, int *_DataRow)
{

assert(_XPos < DataXMax);

//transfer the row of data from the input array to the data array, shifting it
//to avoid the border of non data zero values in the points array

for (int y = 0; y < DataYMax; y++) points[_XPos + 1][y + 1] = _DataRow[y];

}// end of TopographicalMapper::LoadDataRow
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::SetDataPoint
//
// Loads a single data point into the data array at position _XPos, _YPos.
//
// Since the data is stored in an array that is actually two elements larger
// in both the x and y directions, the XPos and YPos are offset by one before
// storing into the array.  Thus the user sees the data as starting at position
// 0 but it is actually stored starting at position 1.
//

void __fastcall TopographicalMapper::SetDataPoint(int _XPos, int _YPos,
                                                                    int _Value)
{

assert(_XPos < DataXMax && _YPos < DataYMax);

//transfer the data point from to the data array, shifting it to avoid the
//border of non data zero values in the points array

points[_XPos + 1][_YPos + 1] = _Value;

}//end of TopographicalMapper::SetDataPoint
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
//
// Part III. World-to-Screen coordinate transformation
//
//
// Creating a 2-D image from a 3-D object, one of the easiest to understand,
// and most intuitive methods comes from the concept of a "pin-hole" camera.
// This method came about because it is intuitive for people to model the 3-D
// to 2-D transformation after taking a photograph because, essentially,
// that is the goal of the task, to create a realistic, photographic image
// of a 3-D object.
// An object in 3-D space is usually given as a set of coordinates in the
// world coordinate system, which is just a fancy name for the standard (x,y,z)
// Euclidean space. The goal is to project the 3-D object onto a viewing plane
// in such a way that the image appears realistic.
// The eye coordinate system was defined specifically for that purpose.
// The transformation from world coordinates into eye coordinates is a
// simple multiplication by a 3x3 matrix. When expressed in terms of
// eye coordinates, the object can be projected onto a viewing plane with a
// simple, yet elegant perspective transformation known as the magic M
// transformation. The 2-D image of the object would then be ready for
// display on screen or on paper.

// Basic Definition:

// World Coordinate System
// The first step to modeling an object is to describe it in such a way that
// the computer can store it internally. In order to do that, an object must
// be digitized so that the computer understands the position and relative
// scale of the features of an object. This digitizing step is usually carried
// out by picking a reference point and specifying the features of an object
// relative to the reference point. The reference point is considered the
// origin, and all measurements are taken in 3-D Euclidean space
// (E3 for you math majors) in terms of X, Y, and Z components.
// This coordinate system is considered the world coordinate system.
// In our problem, the input data is set to the value of Z, and the value of X,Y
// is assigned by the program.
//
//
// Pin-Hole Camera
// When modeling the 3-D viewing transformation using the concept of taking
// a photograph, it is useful to consider how a camera works.
// First, assume that the camera is located at a single point in space.
// When taking a picture, the camera lens projects light from a certain
// "viewing volume" onto the film, forming an image. The viewing volume and
// the projection depends on the lens shape. To model the camera for the
// viewing transformation, a pyramid-shaped viewing volume is defined using
// three parameters: the near clipping plane, the far clipping plane, and
// the viewing angle. The eye is at the apex of the pyramid, and the two
// clipping planes are perpendicular to the line of sight, which runs from the
// eye through the center of the two clipping planes.
// The view angle is defined as the angle formed by the opposing walls
// of the viewing volume. The viewing volume can be illustrated with the
// following diagram.
//      ^
//     Y|  ^         Near Clipping Plan
//      | /X         |
//      ./__         |          (   .  EYE
//      |  Z         |          View Angle
//      |
//      Far clipping Plan
//
// Eye Coordinate System
// A coordinate system defined on the viewing volume. The origin is at the tip
// of the pyramid. The positive Z axis, which is the line of sight of the
// pin-hole camera, runs down the center of the pyramid. The positive Y axis
// points up looking down the Z axis. The positive X axis points to the right
// looking down the Z axis. This is a left-handed coordinate system.

// World to Eye Tranformation
// which is to take an object specified in world coordinates and transform it
// into eye coordinates. This transformation is essentially a translation and
// several rotations, and it leaves the shape and relative size of the object
// unchanged. In order to perform the transformation, the relationship between
// the eye coordinate system and the world coordinate system must be defined.

// This step is done as follows. First, a FROM POINT is specified in world
// coordinates. This point is where the viewer positions his eye to look
// at the object. Then an AT POINT is defined in world coordinates.
// The AT POINT along with the FROM POINT defines the line of sight.
// Then, an UP VECTOR is defined in world coordinates. The UP VECTOR determines
// the orientation of the view volume. Finally, a viewing angle is defined to
// specify the breadth of the view volume.
// When these parameters have been specified, the transformation from world
// coordinates to eye coordinates is fixed. The from point is mapped to the
// origin of the eye coordinate space. The at point determines the positive
// Z axis in eye coordinate space. The up vector, along with the Z axis
// determines the X axis. The Z and X axes determine the Y axis. Finally,
// the view angle determines the span of the X and Y axes in the viewing
// volume as a function of Z (distance from origin).
// There are three requirements on the parameters:
// The at point cannot be the same as the from point.
// The up vector cannot be parallel to the line of sight.
// The view angle must be less than 180 degrees.
//
// Eye to 2-D (magic M) Transformation
// The objects in the view volume are visible to the eye, and when a "picture"
// is taken, they are projected onto a viewing plane that is parallel to the
// clipping planes. This projection is a perspective projection, that is far
// objects appear smaller than near objects of the same size. This
// transformation is performed by setting Xi to Xeye/Zeye and Yi to Yeye/Zeye,
// where Xi and Yi are the 2-D coordinates of the image. This mapping projects
// everything in the viewing volume to a plane at Zeye = 1 where the boundary
// points are (1,1), (1,-1), (-1,-1), and (-1,1). The image on the plane can
// then be drawn on a screen or paper.
//
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::WorldToScreen
//
// This function is used to set the value of world coordinate for the map,
// and transform the world coordinate to the screen coordinate.
//

void __fastcall TopographicalMapper::WorldToScreen(int _Degree, int _StretchX,
                                                    int _StretchY)
{

double co, si, tmp1, tmp2;

// int maxZ; //Z-axis length

co = cos ( PI * (double)Degree / 180.0 );
si = sin ( PI * (double)Degree / 180.0 );

for ( int i = 0; i < XMax; i++)
    for ( int j = 0; j < YMax; j++){

        // set value (x,y,z) of every point in the world coordinate
        Vertex p;
        p.x = (i - XMax / 2.0) * _StretchX;
        p.y = (j - YMax / 2.0) * _StretchY;
        p.z = points[i][j];

        // set value of orthograghic point of p
        Vertex orthoP;
        orthoP.x = (i - XMax / 2.0) * _StretchX;
        orthoP.y = (j - YMax / 2.0) * _StretchY;
        orthoP.z = 0;

        // Rotation Z-axis
        tmp1 = p.x * co - p.y * si;
        tmp2 = p.x * si + p.y * co;
        p.x=tmp1;
        p.y=tmp2;

        // Rotation Z-axis
        tmp1 = orthoP.x * co - orthoP.y * si;
        tmp2 = orthoP.x * si + orthoP.y * co;
        orthoP.x=tmp1;
        orthoP.y=tmp2;

        // get value of screen plane points
        s[i][j] = vTrans3Dto2D(p);
        orthoS[i][j]=vTrans3Dto2D(orthoP);
        }
}
// end of TopographicalMapper::WorldToScreen
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
//TopographicalMapper::vTrans3Dto2D
//
// This function is used to transform the 3D world coordinate to the 2D screen
// coordinate using the magic transformation matrix m[][].
//

ScreenPlane __fastcall TopographicalMapper::vTrans3Dto2D(Vertex pSP)
{

ScreenPlane screenPlane;
double vx, vy, vz;
double ex, ey, ez;
// Transform each vertex to eye coord.

vx = pSP.x - fx;
vy = pSP.y - fy;
vz = pSP.z - fz;
ex = vx * m [0][0] + vy * m [0][1] + vz * m [0][2];
ey = vx * m [1][0] + vy * m [1][1] + vz * m [1][2];

//    ez = vx * m [2][0] + vy * m [2][1] + vz * m [2][2];
// Orthographic view volume. In Ortho, ez is a constant    --fqz
ez = RESOLUTION;

//  Translate to screen coordinates.
screenPlane.x = (cx1 * ex / ez + cx2 + ROUND)+ CENTRE_X+dx;
screenPlane.y = Yres-(cy1 * ey / ez + cy2 + ROUND)+ CENTRE_Y+dy;
ez = cz1 - cz2 / ez;

// Count Z buffer
//    if (ez < 0)        ez = 0;
//    if (ez > 65535.0)  ez = 65535;

screenPlane.zDepth = ez;

return screenPlane;

}// end of TopographicalMapper::::vTrans3Dto2D
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::Calculate
//
// This function is used to calculate the value of magic matrix m[3][3],
// which is related to From Point (fx, fy, fz) and At Point (ax, ay, az).
//

void __fastcall TopographicalMapper::Calculate(int _az, int _dAngle)
{

double    norm;
Vertex   p1, p2, p3, temp;
az = _az;
dAngle = _dAngle;

//protect map data
if(az > 25) az = 25; if(dAngle <= -6) dAngle = -5;

// World-to-Eye transformation
// Get transformation Matirx
// Compute Z axis
p1.x = ax - fx;
p1.y = ay - fy;
p1.z = az - fz;
norm = sqrt (p1.x * p1.x + p1.y * p1.y + p1.z * p1.z);

if (norm!=0) {
    m [2][0] = p1.x / norm;
    m [2][1] = p1.y / norm;
    m [2][2] = p1.z / norm;
    }
else
    MessageDlg("Calc Znorm=0",mtInformation, TMsgDlgButtons()<<mbOK,0);

//compute X axis
temp.x = ux;
temp.y = uy;
temp.z = uz;
vCross (p1, temp, p2);

norm = sqrt (p2.x * p2.x + p2.y * p2.y + p2.z * p2.z);
if (norm!=0){
    m [0][0] = p2.x / norm;
    m [0][1] = p2.y / norm;
    m [0][2] = p2.z / norm;
    }
else
    MessageDlg("Calc Xnorm = 0", mtInformation, TMsgDlgButtons()<<mbOK, 0);

//compute Y axis
if(norm!=0){
    vCross (p2, p1, p3);
    norm = sqrt (p3.x * p3.x + p3.y * p3.y + p3.z * p3.z);
    m [1][0] = p3.x / norm;
    m [1][1] = p3.y / norm;
    m [1][2] = p3.z / norm;
    }
else
    MessageDlg("Calc Ynorm = 0", mtInformation, TMsgDlgButtons()<<mbOK, 0);

// Magic M transformation
norm = 2.0 * tan ((angle+dAngle) * 3.14159265 / 360.0);
cx1 = Xres /  norm;
cx2 = Xres / 2.0;
cy1 = Yres / norm;
cy2 = Yres / 2.0;
cz1 = 65535.0 * zFar / (zFar - zNear);
cz2 = cz1 * zNear;

}//end of TopographicalMapper::Calculate
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::vCross
//

void  __fastcall TopographicalMapper::vCross(Vertex &_p1, Vertex &_p2,
                                                                    Vertex &_p)
{
    _p.x = _p1.y * _p2.z - _p2.y * _p1.z;
    _p.y = _p2.x * _p1.z - _p1.x * _p2.z;
    _p.z = _p1.x * _p2.y - _p2.x * _p1.y;

}// end of TopographicalMapper::::vCross
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::Paint
//
// This function is used to receive parameters sent by the caller function,
// and call all functions to paint a map on the image, then transfer this image
// to the paintbox.
// To avoide the flicker problem, we first plot the polygons and lines on to a
// image, which is invisible to viewer, then transfer the image to a bitmap,
// finally, draw this bitmap on the paintbox, which is visible to the viewer.
//

void __fastcall TopographicalMapper::Paint(int _az, int _dx, int _dy,
                int _dAngle, int _degree, int _StretchX, int _StretchY,
                bool _HiddenSurfaceViewMode, bool _WireFrameViewMode,
                bool _BirdsEyeViewMode,
                int _criticalValue, int _warnValue, int _normalValue)
{

//if data arrays have not yet been created, do nothing
if (points == NULL) return;

//store view parameters

dx =_dx; dy =_dy; //screen position
Degree = _degree;
StretchX = _StretchX; StretchY = _StretchY;

criticalValue = _criticalValue;
warnValue = _warnValue;
normalValue = _normalValue;

HiddenSurfaceViewMode = _HiddenSurfaceViewMode; //show hidden line view if true
WireFrameViewMode = _WireFrameViewMode; //show wire frame view if true
BirdsEyeViewMode = _BirdsEyeViewMode; //show bird's eye view if true

//Paint a rectangle with the size of paintbox
TRect rect = paintbox->ClientRect;
image->Canvas->Brush->Color = clWhite;
image->Canvas->FillRect(rect);

// calculate the mapping parameters and the magic matrix
Calculate(_az, _dAngle);
// 3D-2D coordinate transformation
WorldToScreen(Degree, StretchX, StretchY);
// calculate the mouse active zone to show the peak of value
MouseImage();

//create the image on a hidden canvas (to avoid flicker)
if(HiddenSurfaceViewMode) HiddenSurfaceDraw();
if(WireFrameViewMode) DrawWireFrame();
if(BirdsEyeViewMode) BirdsEyeView();

//transfer the hidden image to the TBitmap object
TransferBitmap->Assign(image->Picture->Bitmap);

//transfer the TBitmap image to the paintbox canvas
paintbox->Canvas->Draw(0, 0, TransferBitmap);

}//end of TopographicalMapper::Paint
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::BirdsEyeView
//
// This function is used to show a bird's eye view of the input data.
// In other words, the top view of the orthograhic projection.
//

void __fastcall TopographicalMapper::BirdsEyeView(void)
{

for ( int i = 0; i < XMax; i++){

    image->Canvas->Pixels[850-i*10][0] =  clBlue;

    for ( int j = 0; j < YMax; j++)
        if (points[i][j] > THRESHOLD)
            image->Canvas->Pixels[850-i*10][j*10] =  clRed;

    }

} // end of TopographicalMapper::BirdsEyeView
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::DrawWireFrame
//
// This function is used to draw the wireframe of the input data.
//

void __fastcall TopographicalMapper::DrawWireFrame(void)
{

//draw data points, on the image, which is connected with lines.
for ( int i = 0; i < XMax - 1 ; i++)
    for ( int j = 0; j < YMax - 1 ; j++){

        image->Canvas->MoveTo(s[i][j].x, s[i][j].y);
        image->Canvas->LineTo(s[i+1][j].x, s[i+1][j].y);
        image->Canvas->MoveTo(s[i][j].x, s[i][j].y);
        image->Canvas->LineTo(s[i][j+1].x, s[i][j+1].y);
        }

// draw the left border.
for ( int i = 0; i < XMax - 1 ; i++) {
    image->Canvas->MoveTo(s[i][YMax -1].x, s[i][YMax -1].y);
    image->Canvas->LineTo(s[i+1][YMax -1].x, s[i+1][YMax -1].y);
    }

}//end of DrawWireFrame
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::HiddenSurfaceDraw
//
// This function is used to draw data points on the image with hidden surface
// removal.
//
// Polygonal representation of three-dimensional objects is the classic
// representational form in three-dimensional graphics. An object is represented
// by a mesh of polygonal facets. In the general cases an object possesses
// curved surfaces and the facets could be represented by several polygons. In
// our problem, we generate the map using the polygon modelling method with
// the mathematical description.
//
// From the wire frame map, we generalize that the map could be decomposed into
// many triangles and quadrilaterals. With BCB graphical functions, such as,
// Canvas->Polygon(), we could plot the polygons easily. Anothor important
// thing to be considered is the hidden surface removal. Basically, if we
// draw an object, like the pyramid, we need draw the back facet first, then
// draw the front facet, so the viewer can only see the front facet. This
// order will never change. But in practice, the order, which one is front
// facet and which one is back facet, will be changed according to the change
// of coordinate. So when we change the rotation degree, the coordinate will
// change, and the order of drawing facets must change to suit the hidden
// surface removal requirement. In this program, we change the order of drawing
// facets every 45 degree. In addition, in every 45 degree mapping operation,
// we must decide to draw row(X) first or column(Y) first. It is determined by
// the visual effect. In general, we draw the pyramids (triangles) first, then
// quadrilaterals.
//

void __fastcall TopographicalMapper::HiddenSurfaceDraw(void)
{

// draw the grid without peaks - flat plane

for ( int i = 0; i < XMax - 1; i++)
    for ( int j = 0; j < YMax - 1; j++){
        image->Canvas->MoveTo(orthoS[i][j].x, orthoS[i][j].y);
        image->Canvas->LineTo(orthoS[i+1][j].x, orthoS[i+1][j].y); //plot row
        image->Canvas->MoveTo(orthoS[i][j].x, orthoS[i][j].y);
        image->Canvas->LineTo(orthoS[i][j+1].x, orthoS[i][j+1].y); //plot column
        }


// draw the closing edges of the grid

//along the Y axis
for (int i = 0; i < XMax - 1; i++){
    image->Canvas->MoveTo(s[i][YMax - 1].x, s[i][YMax - 1].y);
    image->Canvas->LineTo(s[i + 1][YMax - 1].x, s[i + 1][YMax - 1].y);
    }

//along the X axis
for (int i = 0; i < YMax - 1; i++){
    image->Canvas->MoveTo(s[XMax - 1][i].x, s[XMax - 1][i].y);
    image->Canvas->LineTo(s[XMax - 1][i + 1].x, s[XMax - 1][i + 1].y);
    }

// Uncomment the next two lines to display a vertical line at the 0,0 point
// of the 3D image grid - this is useful when debugging.
// image->Canvas->MoveTo(s[0][0].x, s[0][0].y);
// image->Canvas->LineTo(s[0][0].x, s[0][0].y - 150);


//draw the polygons to create the 3D image
//the non data zero point values along the edges of the grid are also processed
//because they are part of the polygons connected with the edgemost data points

//drawing always starts at the furthest corner from the viewer and commences
//forward towards the viewer so that the front faces cover the back faces -
//the starting point on the grid and direction of drawing depend on the current
//viewing angle so that drawing always starts at the furthest corner

int XStart, XStop, XDirection, YStart, YStop, YDirection;
int XPolyDirection, YPolyDirection;

if ((Degree >= 0 && Degree < 45) || (Degree >= 315 && Degree < 360)){
    XStart = 1; XStop = XMax; XDirection = 1; XPolyDirection = -1;
    YStart = 1; YStop = YMax; YDirection = 1; YPolyDirection = -1;
    }
else
if (Degree >= 45 && Degree < 135){
    XStart = 1; XStop = XMax; XDirection = 1; XPolyDirection = -1;
    YStart = YMax - 2; YStop = -1; YDirection = -1; YPolyDirection = 1;
    }
else
if (Degree >= 135 && Degree < 225){
    XStart = XMax - 2; XStop = -1; XDirection = -1; XPolyDirection = 1;
    YStart = YMax - 2; YStop = -1; YDirection = -1; YPolyDirection = 1;
    }
else
if (Degree >= 225 && Degree < 315){
    XStart = XMax - 2; XStop = -1; XDirection = -1; XPolyDirection = 1;
    YStart = 1; YStop = YMax; YDirection = 1; YPolyDirection = -1;
    }
else
    return;


//draw all the polygons to create the 3D image - the polygons are drawn on the
//hidden image and need to be transferred to the visible paintbox afterwards

DrawPolygons(image->Canvas, XStart, XStop, XDirection, XPolyDirection,
                             YStart, YStop, YDirection, YPolyDirection, false);

}// end of TopographicalMapper::HiddenSurfaceDraw
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::DrawPolygons
//
// Draws the polygons to create the 3D image.  The polygons are drawn on the
// TCanvas object pointed by _Canvas.
//
// XStart/XStop/XDirection specifies the X index of the data points at which to
// start and stop drawing and the direction of iteration for that index.
// XPolyDirection specifies the direction from the base point of the other
// points to use in forming a quadrilateral relative to the X index.
//
// YStart/YStop/YDirection specifies the Y index of the data points at which to
// start and stop drawing and the direction of iteration for that index.
// YPolyDirection specifies the direction from the base point of the other
// points to use in forming a quadrilateral relative to the Y index.
//
// To ensure that polygon faces at the front of the view hide those on the back
// side, parameters should specify that drawing starts from the opposite corner
// of the data grid with respect to the position of the viewer.
//
// XPolyDirection and YPolyDirection should specify a direction towards the
// opposite corner.  The first point of each polygon will thus be closest to
// the viewer and the remaining points will be towards the opposite corner
// of the grid.
//
// The parameters must be selected for the current rotation angle of the view.
// See the HiddenSurfaceDraw function for examples of parameters for the
// different ranges of angular rotation.
//
// This function is stand-alone so that it may be called to draw a single row
// of data points.  This is useful for high speed data tracking operations
// because it requires less time to draw the grid row by row as new data is
// added rather than redrawing the entire image each time.
//
// If _ClearAreaAboveGrid is true, the area above the grid will be cleared
// to erase any left over graphics from a previous rendering.  This is valid
// only for specific conditions and viewing angles.  It is useful when the
// function is being called by function QuickDrawSingleRow during real time
// data display.  Because this mode does not render and clear the entire screen
// but instead draws one row at a time, old graphics are not cleared above the
// grid plane.  Specifically, the option is valid when viewing the map at a
// rotational angle of 135 degrees and data progression is from left to right.
//
// The function is declared inline to provide the best possible speed when used
// in looping operations.
//

inline void __fastcall TopographicalMapper::DrawPolygons(
                TCanvas *_Canvas,
                int _XStart, int _XStop, int _XDirection, int _XPolyDirection,
                int _YStart, int _YStop, int _YDirection, int _YPolyDirection,
                bool _ClearAreaAboveGrid)

{

int j;

//polygon outlines are black
_Canvas->Pen->Color = clBlack;

for(int i = _XStart; i != _XStop; i += _XDirection){

    //clear area above the grid plane from the edge to the top of the canvas
    //this erases the "sky" - see function header for more in info
    if(_ClearAreaAboveGrid){

        j = _YStart;

        QuadPoly[0] = Point(s[i][j + _YPolyDirection].x,
                                                  s[i][j + _YPolyDirection].y);
        QuadPoly[1] = Point(s[i + _XPolyDirection][j + _YPolyDirection].x,
                                s[i + _XPolyDirection][j + _YPolyDirection].y);
        QuadPoly[2] =  Point(s[i + _XPolyDirection][j + _YPolyDirection].x, 0);
        QuadPoly[3] =  Point(s[i][j + _YPolyDirection].x, 0);

        //erase to standard window color
        _Canvas->Brush->Color = clWindow; _Canvas->Pen->Color = clWindow;

        //draw the quadrilateral on the hidden image canvas
        _Canvas->Polygon(QuadPoly, 3);

        //polygon outlines are black - return pen color to black
        _Canvas->Pen->Color = clBlack;

        }


    for (j = _YStart; j != _YStop; j += _YDirection){

        //assign the points around the current point to a quadrilateral
        //(the other three points are toward the far sides of the grid
        // from the viewer)
        QuadPoly[0] = Point(s[i][j].x, s[i][j].y);
        QuadPoly[1] = Point(s[i + _XPolyDirection][j].x,
                                s[i + _XPolyDirection][j].y);
        QuadPoly[2] = Point(s[i + _XPolyDirection][j + _YPolyDirection].x,
                                s[i + _XPolyDirection][j + _YPolyDirection].y);
        QuadPoly[3] = Point(s[i][j + _YPolyDirection].x,
                                s[i][j + _YPolyDirection].y);

        //get the height for each point - these are the values from the
        //original data input array - the viewing array x,y points are
        //distorted to show the heights as a 3D image on a 2D screen and
        //thus do not make sense for this purpose
        Height[0] = points[i][j];
        Height[1] = points[i + _XPolyDirection][j];
        Height[2] = points[i + _XPolyDirection][j + _YPolyDirection];
        Height[3] = points[i][j + _YPolyDirection];

        //assign a color to the quadrilateral based on the height of its
        //highest point
        _Canvas->Brush->Color = AssignColor(Height);

        //draw the quadrilateral on the hidden image canvas
        _Canvas->Polygon(QuadPoly, 3);

        }

    }

}// end of TopographicalMapper::DrawPolygons
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::AssignColor
//
// Determines the color for a polygon based on the height of its highest
// corner point.  The heights of the four corners are passed via the array
// _Height.
//

TColor __fastcall TopographicalMapper::AssignColor(int Height[4])
{

//color for the polygon
TColor PolyColor;

if (Height[0] >= criticalValue || Height[1] >= criticalValue
   || Height[2] >= criticalValue || Height[3] >= criticalValue)
    PolyColor = clRed;
else
if (Height[0] >= warnValue || Height[1] >= warnValue
   || Height[2] >= warnValue || Height[3] >= warnValue)
    PolyColor = clYellow;
else
if (Height[0] > normalValue || Height[1] > normalValue
   || Height[2] > normalValue || Height[3] > normalValue)
    PolyColor = clGreen;
else
if (Height[0] > 0 || Height[1] > 0 || Height[2] > 0 || Height[3] > 0)
    PolyColor = clBtnFace;
else
    PolyColor = clWindow;

return PolyColor;

}//end of TopographicalMapper::AssignColor
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::QuickDrawSingleRow
//
// Draws a single row of polygons for the points in the data grid row specified
// by _DataY.  The current values for viewing angle and other parameters are
// used, so the map should already have been drawn once using Paint to set
// these parameters as desired.
//
// The polygons are drawn directly on the visible canvas to improve speed. The
// parameters _XStart, _XStop, _XDirection, _XPolyDirection, _YStart, _YStop,
//  _YDirection, _YPolyDirection are described in the DrawPolygons function
//  header.  These parameters are dependent on the current viewing angle.
//
// This function is most useful for real time graphing when new data points are
// added to the grid as they are obtained.  Redrawing the entire map is time
// consuming while drawing a single row is more efficient.
//

void __fastcall TopographicalMapper::QuickDrawSingleRow(
                int _XStart, int _XStop, int _XDirection, int _XPolyDirection,
                int _YStart, int _YStop, int _YDirection, int _YPolyDirection,
                bool _ClearAreaAboveGrid)
{


//perform 3D-2D coordinate transformation so any new data points are processed
//using the previously set viewing parameters
WorldToScreen(Degree, StretchX, StretchY);

//draw all the polygons to create the 3D image - the polygons are drawn directly
//onto the visible canvas so it is not necessary to transfer the hidden image

DrawPolygons(paintbox->Canvas, _XStart, _XStop, _XDirection, _XPolyDirection,
            _YStart, _YStop, _YDirection, _YPolyDirection, _ClearAreaAboveGrid);

}//end of TopographicalMapper::QuickDrawSingleRow
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::SaveJPEG
//
// Saves map as a JPEG image to the file selected by the user.
//

void __fastcall TopographicalMapper::SaveJPEG(void)
{

TSaveDialog *SaveDialog1=new TSaveDialog(paintbox);

TJPEGImage *jp = new TJPEGImage();

try{
    SaveDialog1->DefaultExt = ".jpg";
    SaveDialog1->Filter = "jpg (*.jpg)|*.JPG";
    SaveDialog1->Options << ofOverwritePrompt
                                          << ofFileMustExist << ofHideReadOnly;

    SaveDialog1->FileName = fileName;
    SaveDialog1->InitialDir = ExtractFilePath(fileName);
    jp->Assign(image->Picture->Bitmap);

    if (SaveDialog1->Execute()){
        jp->SaveToFile(SaveDialog1->FileName);
        fileName = SaveDialog1->FileName;
        }
    }
__finally{
    delete jp;
    }

}//end of TopographicalMapper::SaveJPEG
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::MouseImage
//
// This function is used to calculate the size of mouse active zone in which
// the mouse could show the value of peaks.
//

void __fastcall TopographicalMapper::MouseImage(void)
{
imageRight=s[0][0].x;
imageLeft=s[0][0].x;
imageTop=s[0][0].y;
imageBottom=s[0][0].y;
for(int i=0;i<XMax;i++)
    for(int j=0;j<YMax;j++){
        if(s[i][j].x > imageRight) imageRight=s[i][j].x;
        if(s[i][j].x < imageLeft) imageLeft=s[i][j].x;
        if(s[i][j].y > imageBottom) imageBottom=s[i][j].y;
        if(s[i][j].y < imageTop) imageTop=s[i][j].y;
        }

}//end of TopographicalMapper::MouseImage
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::MouseDown
//
// Handle mouse button down events.
//

void __fastcall TopographicalMapper::MouseDown(TMouseButton Button)
{

//Left Mouse Button pressed
if(Button==mbLeft){
    mouseDown=true;
    }
//Right Mouse Botton pressed
if(Button==mbRight){
    rmouseDown=true;
    }

}//end of TopographicalMapper::MouseDown
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::MouseUp
//
// Handle mouse button release events.
//

void __fastcall TopographicalMapper::MouseUp(TMouseButton Button)
{

if(Button==mbLeft) mouseDown=false;

if(Button==mbRight) rmouseDown=false;

}//end of TopographicalMapper::MouseUp
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// TopographicalMapper::MouseMove
//
// Show the value of peaks if the mouse in the region of map.
// The rotation degree will be changed if the left button is pushed
// with mouse moving.  The visual height of peaks will be changed if
// the right button is pushed with mouse moving.
//

void __fastcall TopographicalMapper::MouseMove(int X, int Y)
{

//If mouse moves beyond the mapping zone, Label1 disappears.
if(X<imageLeft || X>imageRight || Y<imageTop || Y>imageBottom){
    Label1->Visible=true;
    }

//If mouse moves within the mapping zone, Label1 shows the value of peak.
if(X > imageLeft && X < imageRight && Y > imageTop && Y < imageBottom)
    for(int i = 0;i < XMax; i++)
        for(int j = 0;j < YMax; j++){
            Label1->Color = clInfoBk;
            if(abs(s[i][j].x-X) < 10 && abs(s[i][j].y-Y) < 10){
                if(points[i][j]>0)
                    Label1->Visible = true;
                if(points[i][j]==0)
                    Label1->Visible = false;

                Label1->Caption=points[i][j];
                Label1->Top=Y+10;
                Label1->Left=X+10;
                }
	    }

//handle left mouse button is down
if (mouseDown){

    if (X <= zone.Left || Y <= zone.Top || X >= zone.Right || Y >= zone.Bottom)
        mouseDown = !mouseDown;

    Degree = (X) * 360 / (zone.Right);

    if(Degree >= 360) Degree = 360; if(Degree <= 0) Degree = 0;

    Paint(az, dx, dy, dAngle, Degree, StretchX, StretchY,
             HiddenSurfaceViewMode, WireFrameViewMode, BirdsEyeViewMode,
             criticalValue, warnValue, normalValue);
    }

//handle right mouse button is down
if (rmouseDown){

    if (X <= zone.Left || Y <= zone.Top || X >= zone.Right || Y >= zone.Bottom)
        rmouseDown = !rmouseDown;

    az = Y*25 / (zone.Bottom-zone.Top);
    if(az > 25) az=25;
    Paint(az, dx, dy, dAngle, Degree, StretchX, StretchY,
             HiddenSurfaceViewMode, WireFrameViewMode, BirdsEyeViewMode,
             criticalValue, warnValue, normalValue);
    }

}
//end of TopographicalMapper::MouseMove
//---------------------------------------------------------------------------

*/

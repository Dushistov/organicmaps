#import "MWMMapDownloaderMode.h"
#import "MWMViewController.h"
#import "MWMMyPositionMode.h"

#import <CoreApi/MWMUTM.h>

@class MWMWelcomePageController;
@class MWMMapViewControlsManager;
@class EAGLView;
@class MWMMapDownloadDialog;
@class BookmarksCoordinator;
@protocol MWMLocationModeListener;

@interface MapViewController : MWMViewController

+ (MapViewController *_Nullable)sharedController;
- (void)addListener:(id<MWMLocationModeListener>_Nonnull)listener;
- (void)removeListener:(id<MWMLocationModeListener>_Nonnull)listener;

// called when app is terminated by system
- (void)onTerminate;
- (void)onGetFocus:(BOOL)isOnFocus;

- (void)updateStatusBarStyle;

- (void)performAction:(NSString *_Nonnull)action;

- (void)openMapsDownloader:(MWMMapDownloaderMode)mode;
- (void)openEditor;
- (void)openBookmarkEditor;
- (void)openFullPlaceDescriptionWithHtml:(NSString *_Nonnull)htmlString;
- (void)showBookmarksLoadedAlert:(UInt64)categoryId;
- (void)openCatalogAnimated:(BOOL)animated utm:(MWMUTM)utm;
- (void)openCatalogDeeplink:(NSURL *_Nonnull)deeplinkUrl animated:(BOOL)animated utm:(MWMUTM)utm;
- (void)openCatalogAbsoluteUrl:(NSURL *_Nullable)url animated:(BOOL)animated utm:(MWMUTM)utm;
- (void)searchText:(NSString *_Nonnull)text;
- (void)openDrivingOptions;

- (void)showRemoveAds;
- (void)setPlacePageTopBound:(CGFloat)bound duration:(double)duration;

+ (void)setViewport:(double)lat lon:(double)lon zoomLevel:(int)zoomlevel;

- (void)initialize;
- (void)enableCarPlayRepresentation;
- (void)disableCarPlayRepresentation;

- (void)dismissPlacePage;

@property(nonatomic, readonly) MWMMapViewControlsManager * _Nonnull controlsManager;
@property(nonatomic) MWMWelcomePageController * _Nullable welcomePageController;
@property(nonatomic, readonly) MWMMapDownloadDialog * _Nonnull downloadDialog;
@property(nonatomic, readonly) BookmarksCoordinator * _Nonnull bookmarksCoordinator;

@property(nonatomic) MWMMyPositionMode currentPositionMode;
@property(strong, nonatomic) IBOutlet EAGLView * _Nonnull mapView;
@property(strong, nonatomic) IBOutlet UIView * _Nonnull controlsView;
@property(strong, nonatomic) IBOutlet UIView * _Nonnull searchViewContainer;

@end

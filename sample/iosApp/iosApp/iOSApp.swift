import SwiftUI
import shared

@main
struct iOSApp: App {
    
    let sharedApp = ElastikSampleAppComponent()
    
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}

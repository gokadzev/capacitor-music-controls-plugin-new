
  Pod::Spec.new do |s|
    s.name = 'CapacitorMusicControlsPluginNew'
    s.version = '2.0.0'
    s.summary = 'Implementation of MusicControls for Capacitor 7 projects - Android 15 Compatible'
    s.license = 'MIT'
    s.homepage = 'https://github.com/gokadzev/capacitor-music-controls-plugin-new'
    s.author = 'gokadzev'
    s.source = { :git => 'https://github.com/gokadzev/capacitor-music-controls-plugin-new', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '13.0'
    s.dependency 'Capacitor'
    s.swift_version = '5.0'
  end

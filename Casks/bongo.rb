cask "bongo" do
  arch arm: "arm64", intel: "intel-x86_64"

  version "1.0.0"
  sha256 arm:   "9f876aed808ac05483502bd999ce750e9cbc58bf8dde3f87343c40466e51b353",
         intel: "73b7ccba0d0037bf2087da4c40fa27efb03c3fd06de9862f8d7fdf861d4a58d0"

  url "https://github.com/mehedishakeel/Bongo/releases/download/v1.0.0/Bongo-1.0-macos-#{arch}.dmg"
  name "Bongo"
  desc "Native Bangla Phonetic Keyboard for macOS"
  homepage "https://getbongo.app/"

  livecheck do
    url :url
    strategy :github_latest
  end

  auto_updates true
  depends_on macos: :ventura

  app "Bongo.app"

  zap trash: [
    "~/Library/Application Support/Bongo",
    "~/Library/Caches/com.mehedishakeel.bongo",
    "~/Library/Preferences/com.mehedishakeel.bongo.plist",
  ]
end

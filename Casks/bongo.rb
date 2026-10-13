cask "bongo" do
  arch arm: "arm64", intel: "intel-x86_64"

  version "1.0.0"
  sha256 arm:   "b42b42d54a8eeee1903b30881305edde008d2ee03cd677a91ebacffb88149073",
         intel: "90088cd39eacdfe72f6d33b5d2bda1b3375307c843806e4a5882e8e789779566"

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
    "~/Library/Caches/org.bongo.Bongo",
    "~/Library/Caches/org.bongo.inputmethod.Bongo",
    "~/Library/Preferences/org.bongo.Bongo.plist",
    "~/Library/Preferences/org.bongo.inputmethod.Bongo.plist",
  ]
end

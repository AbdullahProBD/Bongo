"""Validate platform identity isolation and resource integrity across macOS, Linux, and Windows."""
import plistlib
from pathlib import Path
import unittest
import json

ROOT = Path(__file__).resolve().parents[1]

class DistributionTests(unittest.TestCase):
    def test_macos_registration(self):
        p = plistlib.loads((ROOT / 'platforms/macos/Bongo/Resources/Info.plist').read_bytes())
        self.assertEqual(p['CFBundleIdentifier'], 'org.bongo.inputmethod.Bongo')
        self.assertEqual(p['InputMethodServerControllerClass'], 'BongoInputController')
        self.assertEqual(p['TISIntendedLanguage'], 'bn')
        self.assertEqual(p['TISInputSourceID'], p['CFBundleIdentifier'])
        self.assertEqual(p['LSArchitecturePriority'], ['arm64'])
        controller = (ROOT / 'platforms/macos/Bongo/Sources/InputController.swift').read_text()
        self.assertIn('@objc(' + p['InputMethodServerControllerClass'] + ')', controller)
        main = (ROOT / 'platforms/macos/Bongo/Sources/main.swift').read_text()
        self.assertIn(p['InputMethodConnectionName'], main)
        welcome = (ROOT / 'platforms/macos/Bongo/Sources/WelcomeWindow.swift').read_text()
        self.assertIn('final class SidebarNavigationButton: NSButton', welcome)
        self.assertIn('acceptsFirstMouse(for event:', welcome)
        for title in ['Getting Started', 'Bongo Layout', 'Settings', 'Fonts']:
            self.assertIn('"' + title + '"', welcome)
        self.assertNotIn('"Avro Layout"', welcome)
        self.assertNotIn('On-device typing', welcome)
        self.assertIn('final class FontsView: NSView', welcome)
        self.assertIn('BongoFontSettings.font()', (ROOT / 'platforms/macos/Bongo/Sources/CandidatePanel.swift').read_text())
        self.assertTrue(p['TISIconIsTemplate'])
        icon = ROOT / 'platforms/macos/Bongo/Resources' / (p['tsInputMethodIconFileKey'] + '.pdf')
        self.assertTrue(icon.is_file())

        launcher_plist = plistlib.loads((ROOT / 'platforms/macos/Launcher/Resources/Info.plist').read_bytes())
        self.assertEqual(launcher_plist['CFBundleIdentifier'], 'org.bongo.Bongo')
        self.assertEqual(launcher_plist['LSArchitecturePriority'], ['arm64'])
        self.assertNotIn('LSUIElement', launcher_plist)
        launcher = (ROOT / 'platforms/macos/Launcher/Sources/main.swift').read_text()
        self.assertIn('org.bongo.inputmethod.Bongo', launcher)
        self.assertIn('Contents/Library/Input Methods/Bongo Input Method.app', launcher)
        self.assertIn('installInputMethod(from:', launcher)
        self.assertIn('CFBundleVersion', launcher)
        self.assertIn('configuration.arguments = ["--show-settings"]', launcher)
        self.assertIn('CommandLine.arguments.contains("--show-settings")', (ROOT / 'platforms/macos/Bongo/Sources/AppDelegate.swift').read_text())

    def test_linux_registration(self):
        # IBus component XML verification
        ibus_xml = (ROOT / 'platforms/linux/ibus/bongo.xml').read_text()
        self.assertIn('<name>org.freedesktop.IBus.Bongo</name>', ibus_xml)
        self.assertIn('<name>bongo</name>', ibus_xml)
        self.assertIn('<language>bn</language>', ibus_xml)
        self.assertIn('<exec>/usr/libexec/bongo-ibus-engine --ibus</exec>', ibus_xml)

        # Desktop entry & AppStream
        desktop_entry = (ROOT / 'platforms/linux/data/app.getbongo.Bongo.desktop').read_text()
        self.assertIn('Exec=bongo-settings', desktop_entry)
        self.assertIn('Icon=bongo', desktop_entry)
        self.assertIn('StartupWMClass=app.getbongo.Bongo', desktop_entry)

        metainfo = (ROOT / 'platforms/linux/data/app.getbongo.Bongo.metainfo.xml').read_text()
        self.assertIn('<id>app.getbongo.Bongo</id>', metainfo)
        self.assertIn('<name>Bongo</name>', metainfo)

        # Linux UI parity checks (exact same 4 tabs)
        ui_script = (ROOT / 'platforms/linux/ui/bongo_settings.py').read_text()
        for title in ['Getting Started', 'Bongo Layout', 'Settings', 'Fonts']:
            self.assertIn(title, ui_script)
        self.assertIn('Phonetic first', ui_script)
        self.assertIn('Smart suggestions', ui_script)
        self.assertIn('Phonetic only', ui_script)
        self.assertIn('Show emoji in suggestions', ui_script)

        # Linux Engine & scripts
        self.assertTrue((ROOT / 'platforms/linux/engine/Cargo.toml').is_file())
        self.assertTrue((ROOT / 'platforms/linux/scripts/install.sh').is_file())
        self.assertTrue((ROOT / 'platforms/linux/scripts/build-deb.sh').is_file())
        self.assertTrue((ROOT / 'platforms/linux/scripts/build-rpm.sh').is_file())

    def test_windows_registration(self):
        # Windows UI parity checks (exact same 4 tabs)
        ui_script = (ROOT / 'platforms/windows/ui/bongo_settings.py').read_text()
        for title in ['Getting Started', 'Bongo Layout', 'Settings', 'Fonts']:
            self.assertIn(title, ui_script)
        self.assertIn('Phonetic first', ui_script)
        self.assertIn('Smart suggestions', ui_script)
        self.assertIn('Phonetic only', ui_script)
        self.assertIn('Show emoji in suggestions', ui_script)

        # Windows service verification
        service_script = (ROOT / 'platforms/windows/service/bongo_service.py').read_text()
        self.assertIn('WH_KEYBOARD_LL', service_script)
        self.assertIn('SendInput', service_script)
        self.assertIn('bongo_engine', service_script)

        # Windows Engine, installer scripts, and icons
        self.assertTrue((ROOT / 'platforms/windows/engine/Cargo.toml').is_file())
        self.assertTrue((ROOT / 'platforms/windows/engine/src/lib.rs').is_file())
        self.assertTrue((ROOT / 'platforms/windows/installer/bongo.iss').is_file())
        self.assertTrue((ROOT / 'platforms/windows/installer/bongo.wxs').is_file())
        self.assertTrue((ROOT / 'platforms/windows/scripts/build-windows.bat').is_file())
        self.assertTrue((ROOT / 'platforms/windows/scripts/package_windows.py').is_file())
        self.assertTrue((ROOT / 'platforms/windows/data/bongo.ico').is_file())
        self.assertTrue((ROOT / 'platforms/windows/data/bongo.png').is_file())

    def test_platform_architecture_integrity(self):
        # Ensure supported platform directories exist and legacy platforms remain clean
        self.assertTrue((ROOT / 'platforms/macos').is_dir(), 'macOS platform directory must exist')
        self.assertTrue((ROOT / 'platforms/linux').is_dir(), 'Linux platform directory must exist')
        self.assertTrue((ROOT / 'platforms/windows').is_dir(), 'Windows platform directory must exist')
        self.assertFalse((ROOT / 'licenses/GPL-3.0.txt').exists(), 'Linux GPL license should not exist')
        self.assertFalse((ROOT / 'licenses/MPL-1.1.txt').exists(), 'Legacy MPL-1.1 license should not exist')

        # Check create-dmg.sh supports Apple Silicon and Intel targets
        script = (ROOT / 'platforms/macos/scripts/create-dmg.sh').read_text()
        self.assertIn('aarch64-apple-darwin', script)
        self.assertIn('arm64-apple-macos13.0', script)
        self.assertIn('x86_64-apple-darwin', script)
        self.assertIn('x86_64-apple-macos13.0', script)

        # Ensure upstream.lock.json tracks components cleanly
        upstream = json.loads((ROOT / 'upstream.lock.json').read_text())
        self.assertIn('Lekho', upstream)
        self.assertIn('riti-macos', upstream)
        self.assertIn('riti-linux', upstream)
        self.assertIn('riti-windows', upstream)
        self.assertNotIn('OpenBangla-Keyboard', upstream)
        self.assertNotIn('Avro-Keyboard', upstream)

    def test_release_artifact_layout(self):
        for name in ['install.sh', 'uninstall.sh', 'create_dmg.sh']:
            self.assertFalse((ROOT / 'platforms/macos/scripts' / name).exists())
        self.assertFalse((ROOT / 'scripts/Register Bongo.command').exists())
        release = (ROOT / 'platforms/macos/scripts/create-dmg.sh').read_text()
        self.assertNotIn('BongoRegister', release)
        self.assertNotIn('Register Bongo.command', release)
        self.assertIn('$STAGE/Bongo.app', release)
        self.assertNotIn('$STAGE/Bongo Input Method.app', release)
        self.assertIn('Contents/Library/Input Methods/Bongo Input Method.app', release)
        self.assertIn("ln -s '/Applications'", release)
        self.assertNotIn("ln -s '/Library/Input Methods'", release)
        self.assertIn('RELEASE_DIR="$PROJECT_ROOT/release"', release)
        self.assertNotIn('dist/', release)

        self.assertTrue((ROOT / 'platforms/macos/release/.gitkeep').is_file())
        self.assertTrue((ROOT / 'platforms/linux/release/.gitkeep').is_file())
        self.assertTrue((ROOT / 'platforms/windows/release/.gitkeep').is_file())
        mac_scripts = {
            path.relative_to(ROOT).as_posix()
            for path in (ROOT / 'platforms/macos/scripts').glob('*')
            if path.is_file()
        }
        self.assertEqual(mac_scripts, {
            'platforms/macos/scripts/create-dmg.sh',
        })
        self.assertFalse((ROOT / 'scripts').exists())

    def test_updates_use_bongo_release_configuration(self):
        mac = (ROOT / 'platforms/macos/Bongo/Sources/WelcomeWindow.swift').read_text()
        self.assertIn('BongoGitHubRepository', mac)
        self.assertIn('api.github.com/repos/', mac)
        self.assertIn('releases?per_page=1', mac)
        self.assertIn('No newer release is published yet', mac)
        self.assertIn('statusCode == 404', mac)
        self.assertNotIn('openbangla.github.io', mac)
        self.assertNotIn('omicronlab.com/download/liveupdate', mac)

    def test_phonetic_engine_accuracy(self):
        import importlib.util

        # 1. Test Windows Avro Phonetic Engine
        spec_win = importlib.util.spec_from_file_location(
            "bongo_win_service",
            str(ROOT / "platforms/windows/service/bongo_service.py")
        )
        mod_win = importlib.util.module_from_spec(spec_win)
        spec_win.loader.exec_module(mod_win)

        win_eng = mod_win.AvroPhoneticEngine()
        self.assertEqual(win_eng.get_candidates("ami")[0], "আমি")
        self.assertEqual(win_eng.get_candidates("bangla")[0], "বাংলা")
        self.assertEqual(win_eng.get_candidates("kemon")[0], "কেমন")
        self.assertEqual(win_eng.get_candidates("dhonnobad")[0], "ধন্যবাদ")

        # 2. Test Linux Avro Phonetic Engine directly
        spec_lin = importlib.util.spec_from_file_location(
            "bongo_lin_engine",
            str(ROOT / "platforms/linux/ibus/bongo_ibus_engine.py")
        )
        mod_lin = importlib.util.module_from_spec(spec_lin)
        spec_lin.loader.exec_module(mod_lin)

        lin_eng = mod_lin.AvroPhoneticEngine()
        self.assertEqual(lin_eng.get_candidates("ami")[0], "আমি")
        self.assertEqual(lin_eng.get_candidates("bangla")[0], "বাংলা")
        self.assertEqual(lin_eng.get_candidates("kemon")[0], "কেমন")
        self.assertEqual(lin_eng.get_candidates("dhonnobad")[0], "ধন্যবাদ")

        # 3. Test Windows Service Key Processing Lifecycle
        svc = mod_win.BongoWindowsService()
        self.assertFalse(svc.has_session())

        # Feed "ami"
        svc.process_key(0x41, True, False, False, False) # 'a'
        svc.process_key(0x4D, True, False, False, False) # 'm'
        svc.process_key(0x49, True, False, False, False) # 'i'
        self.assertTrue(svc.has_session())
        self.assertEqual(svc.candidates[0], "আমি")

        # Space commits active candidate cleanly
        handled = svc.process_key(mod_win.VK_SPACE, True, False, False, False)
        self.assertTrue(handled)
        self.assertFalse(svc.has_session())

        # Feed "ami" and test Enter (commits without newline)
        svc.process_key(0x41, True, False, False, False)
        svc.process_key(0x4D, True, False, False, False)
        svc.process_key(0x49, True, False, False, False)
        self.assertTrue(svc.has_session())
        h_ret = svc.process_key(mod_win.VK_RETURN, True, False, False, False)
        self.assertTrue(h_ret)
        self.assertFalse(svc.has_session())

        # Feed "ami" and test punctuation (commits candidate, lets punctuation pass through)
        svc.process_key(0x41, True, False, False, False)
        svc.process_key(0x4D, True, False, False, False)
        svc.process_key(0x49, True, False, False, False)
        self.assertTrue(svc.has_session())
        h_punct = svc.process_key(0xBF, True, True, False, False) # '?'
        self.assertFalse(h_punct)
        self.assertFalse(svc.has_session())

        # Test period outside and inside session converts to Dari
        h_dari_alone = svc.process_key(0xBE, True, False, False, False)
        self.assertTrue(h_dari_alone)

        svc.process_key(0x41, True, False, False, False)
        h_dari_session = svc.process_key(0xBE, True, False, False, False)
        self.assertTrue(h_dari_session)
        self.assertFalse(svc.has_session())

        # Test F12 and Ctrl+Space toggle
        self.assertTrue(svc.bangla_active)
        svc.process_key(mod_win.VK_F12, True, False, False, False)
        self.assertFalse(svc.bangla_active)
        svc.process_key(mod_win.VK_F12, True, False, False, False)
        self.assertTrue(svc.bangla_active)

        # Test Ctrl+Space toggle
        svc.process_key(mod_win.VK_SPACE, True, False, True, False)
        self.assertFalse(svc.bangla_active)
        svc.process_key(mod_win.VK_SPACE, True, False, True, False)
        self.assertTrue(svc.bangla_active)

        # Test Shifted uppercase typing (e.g. 'T' for ট vs 't' for ত)
        svc.process_key(0x54, True, True, False, False) # 'T'
        self.assertEqual(svc.candidates[0], "ট")
        svc.clear_session()
        svc.process_key(0x54, True, False, False, False) # 't'
        self.assertEqual(svc.candidates[0], "ত")
        svc.clear_session()

if __name__ == '__main__':
    unittest.main()

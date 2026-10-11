# Releasing Bongo

The official repository is [mehedishakeel/Bongo](https://github.com/mehedishakeel/Bongo).

Builds default to `mehedishakeel/Bongo`. Set the environment variable `BONGO_GITHUB_REPOSITORY=owner/repository` only when packaging a custom fork.

## Release Process

1. **Verify Prerequisites**: Check dependencies listed in [BUILDING.md](BUILDING.md).
2. **Run Integrity Tests**:
   ```sh
   python3 -m unittest discover -s tests -v
   ```
3. **Build All Platform Packages**:
   - macOS: `./platforms/macos/scripts/create-dmg.sh all`
   - Linux: `./platforms/linux/scripts/build-deb.sh`
   - Windows: `platforms\windows\scripts\build-windows.bat`
4. **Generate Checksums**:
   Compute sha256 checksums for each release binary:
   ```sh
   shasum -a 256 <binary> > <binary>.sha256
   ```
5. **Tag & Publish**:
   - Tag the release version as `vX.Y.Z` and push the tag.
   - Create a GitHub Release with `gh release create vX.Y.Z ...` attaching all DMGs, DEBs, EXEs, MSIs, and their corresponding `.sha256` files.
   - Sync the documentation wiki with `wiki/push-wiki.sh`.

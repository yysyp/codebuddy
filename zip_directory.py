#!/usr/bin/env python3
"""
Zip the current directory recursively, ignoring 'target' directories.
The zip file will be created in the parent directory with the same name as the current directory.
"""

import os
import zipfile
from datetime import datetime
from pathlib import Path


# Directories and files to ignore when zipping
IGNORE_LIST = {
    'target',
    'output',
    'ignorefolder',
    'venv',
    'nbbuild',
    'dist',
    'node_modules',
    'nbdist',
    'build',
    '.git',
    '.idea',
    '.gradle',
    '.classpath',
    '.project',
    '.settings',
    'nbbuild',
    '.mvn',
}


def should_ignore(name: str) -> bool:
    """Check if the file/directory should be ignored."""
    return name in IGNORE_LIST


def zip_directory(source_dir: Path, output_zip: Path) -> None:
    """
    Zip a directory recursively, ignoring specified files/directories.
    
    Args:
        source_dir: The directory to zip
        output_zip: The output zip file path
    """
    # Use ZIP_STORED for speed priority (no compression)
    with zipfile.ZipFile(output_zip, 'w', zipfile.ZIP_STORED) as zipf:
        for root, dirs, files in os.walk(source_dir):
            # Filter out ignored directories (modifies dirs in-place to prevent walking into them)
            dirs[:] = [d for d in dirs if not should_ignore(d)]
            
            # Add files to the zip
            for file in files:
                if should_ignore(file):
                    continue
                    
                file_path = Path(root) / file
                # Calculate the relative path inside the zip
                arcname = file_path.relative_to(source_dir.parent)
                zipf.write(file_path, arcname)
                print(f"Added: {arcname}")
    
    print(f"\nZip file created: {output_zip}")
    print(f"Total size: {output_zip.stat().st_size / 1024 / 1024:.2f} MB")


def main():
    # Get current directory
    current_dir = Path.cwd()
    dir_name = current_dir.name
    
    # Create output zip path in parent directory with timestamp
    parent_dir = current_dir.parent
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    output_zip = parent_dir / f"{dir_name}_{timestamp}.zip"
    
    print(f"Source directory: {current_dir}")
    print(f"Output zip file: {output_zip}")
    print(f"Ignoring directories/files: {', '.join(sorted(IGNORE_LIST))}")
    print("-" * 60)
    
    # Check if output zip already exists
    if output_zip.exists():
        response = input(f"Warning: {output_zip} already exists. Overwrite? (y/n): ")
        if response.lower() != 'y':
            print("Operation cancelled.")
            return
        output_zip.unlink()
    
    # Create the zip file
    zip_directory(current_dir, output_zip)


if __name__ == "__main__":
    main()

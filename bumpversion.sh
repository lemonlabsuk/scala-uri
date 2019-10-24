#!/usr/bin/env sh

echo "Running SBT to determine current version. Please wait..."
VER=$(sbt 'project scalaUriJVM' 'show version' | tail -n 1 | cut -f2 -d' ')

echo "Current version is $VER, what is the next version?"
read -r NEW_VER

sed -i '' "s/$VER/$NEW_VER/g" version.sbt
sed -i '' "s/$VER/$NEW_VER/g" README.md
sed -i '' "s/scala.uri.ver=$VER/scala.uri.ver=$NEW_VER/g" .travis.yml

git commit -am"Bump version to $NEW_VER"
git tag $NEW_VER

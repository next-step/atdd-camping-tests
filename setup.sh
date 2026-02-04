#!/usr/bin/env bash
set -e

mkdir -p repos
cd repos

update_or_clone () {
  local dir=$1
  local repo=$2
  local branch=$3

  if [ -d "$dir/.git" ]; then
    echo "🔄 $dir 이미 존재 → pull"
    cd "$dir"
    git fetch origin
    git checkout "$branch"
    git pull origin "$branch"
    cd ..
  else
    echo "📦 $dir clone"
    git clone "$repo" --branch "$branch" --single-branch
  fi
}

update_or_clone atdd-camping-reservation \
  https://github.com/yhh1056/atdd-camping-reservation.git yhh1056

update_or_clone atdd-camping-admin \
  https://github.com/yhh1056/atdd-camping-admin.git yhh1056

update_or_clone atdd-camping-kiosk \
  https://github.com/yhh1056/atdd-camping-kiosk.git yhh1056

echo "✅ 모든 프로젝트 최신 상태로 준비 완료"

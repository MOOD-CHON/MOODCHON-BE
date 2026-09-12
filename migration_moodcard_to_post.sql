-- 무드 카드를 탐색 탭 게시물(posts)로 교체하는 마이그레이션.
-- 로컬 DB와 RDS에 각각 한 번씩 실행한다.
--
-- 배경: 무드 카드가 mood_cards(더미 3장, 태그 0개)를 쓰고 있어 무드 결과가 항상 같게 나왔다.
--       이제 posts(TourAPI 사진 765장 + AI 태그 2667개)를 무드 카드로 쓴다.
--
-- 주의: chonkang_mood_selections.mood_card_id 가 NOT NULL 이라 컬럼을 지우지 않으면
--       새 선택 기록을 저장할 수 없다. post_id 컬럼과 유니크 제약은 앱 기동 시
--       Hibernate(ddl-auto: update)가 만든다.

BEGIN;

-- 기존 선택 기록은 mood_cards를 가리키므로 의미가 없어진다. (로컬 테스트 6행 / RDS 0행)
-- 전체 삭제가 의도다. WHERE 절은 IDE의 "WHERE 없는 DELETE" 경고를 피하려고 붙였을 뿐이다.
-- 비우지 않으면 Hibernate가 NOT NULL 인 post_id 컬럼을 추가할 때 기존 행 때문에 실패한다.
DELETE FROM chonkang_mood_selections WHERE id IS NOT NULL;

-- 컬럼을 지우면 (chonkang_id, user_id, mood_card_id) 유니크 제약과 FK도 함께 사라진다.
ALTER TABLE chonkang_mood_selections DROP COLUMN mood_card_id;

-- 더 이상 쓰지 않는 테이블. 의존 순서대로 지운다.
DROP TABLE mood_card_tags;
DROP TABLE mood_cards;
DROP TABLE accommodation_types;

COMMIT;

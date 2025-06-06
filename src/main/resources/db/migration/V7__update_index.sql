ALTER TABLE listing_access_control_policies
    DROP FOREIGN KEY FK_LISTING_ACCESS_CONTROL_POLICIES_ON_APPLY_TO;

DROP INDEX listings_searchKey_index ON listings;

CREATE UNIQUE INDEX listings_project_id_searchKey_uindex ON listings (project_id, search_key);
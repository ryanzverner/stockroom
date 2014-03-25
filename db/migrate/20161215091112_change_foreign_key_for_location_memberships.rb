Sequel.migration do
  up do
    alter_table(:location_memberships) do
      drop_foreign_key :person_id
      add_foreign_key :employment_id, :employment, :foreign_key_constraint_name => 'location_memberships_fkey_employment_id'
    end
  end

  down do
    alter_table(:location_memberships) do
      add_foreign_key :person_id, :people, :foreign_key_constraint_name => 'location_memberships_fkey_person_id'
      drop_foreign_key :employment_id
    end
  end
end

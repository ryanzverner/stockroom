Sequel.migration do
  up do
    create_table(:location_memberships) do
      primary_key :id

      foreign_key :person_id, :people, :foreign_key_constraint_name => 'location_memberships_fkey_person_id'
      foreign_key :location_id, :locations, :foreign_key_constraint_name => 'location_memberships_fkey_location_id'

      DateTime :start, :null => false
      DateTime :created_at, :null => false
      DateTime :updated_at, :null => false
    end
  end

  down do
    drop_table(:location_memberships)
  end
end

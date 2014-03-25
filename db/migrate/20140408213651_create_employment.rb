Sequel.migration do
  up do
    create_table(:employment) do
      primary_key :id
      DateTime :start
      DateTime :end

      foreign_key :position_id, :positions, :foreign_key_constraint_name => 'employment_fkey_position_id'
      foreign_key :person_id, :people, :foreign_key_constraint_name => 'employment_fkey_person_id'

      DateTime :updated_at
      DateTime :created_at
    end
  end

  down do
    drop_table(:employment)
  end
end

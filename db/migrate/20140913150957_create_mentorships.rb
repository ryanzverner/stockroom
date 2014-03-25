Sequel.migration do
  up do
    create_table(:mentorships) do
      primary_key :id

      foreign_key :mentor_id, :people, :foreign_key_constraint_name => 'mentorship_fkey_mentor_id'
      foreign_key :apprentice_id, :people, :foreign_key_constraint_name => 'mentorship_fkey_apprentice_id'

      DateTime :start
      DateTime :end

      DateTime :updated_at
      DateTime :created_at
    end
  end

  down do
    drop_table(:mentorships)
  end
end
